"""
A module containing Shortest Path Kernel.
"""
import numpy as np

import basic
import threading

from collections import OrderedDict
from pykernels.base import GraphKernel
from scipy.sparse import lil_matrix
from scipy.sparse.csgraph import floyd_warshall


class ShortestPath(GraphKernel):
    """
    Shortest Path kernel [3]
    """

    def __init__(self, labeled=False):
        self.labeled = labeled

    class FW_Thread(threading.Thread):
        def __init__(self, thread_ID, data, i_from, i_to, shortest_path):
            threading.Thread.__init__(self)

            self.threadID = thread_ID
            self.shortest_path = shortest_path

            self.data = data

            self.i_from = i_from
            self.i_to = i_to

        def run(self):
            for i in range(self.i_from, self.i_to):
                print 'Perform FW: ', i
                self.shortest_path._floyd_warshall(self.data[i], self.data[i], i)

    def _floyd_warshall(self, adj_mat, weights, index):
        """
        Returns matrix of shortest path weights.
        """
        res = floyd_warshall(lil_matrix(adj_mat))

        self.floyd_warshall_result[index] = res
        self.max_list[index] = (res[~np.isinf(res)]).max()

        # print 'Finish FW: ', index

    def _apply_floyd_warshall(self, data):
        """
        Applies Floyd-Warshall algorithm on a dataset.
        Returns a tuple containing dataset of FW transformates and max path length
        """
        self.floyd_warshall_result = {}
        self.max_list = np.zeros(len(data))

        max_thread_number = 200
        thread_number = max_thread_number if len(data) > max_thread_number else len(data)
        interval = int(len(data) / thread_number)
        threads = []

        for i in range(0, thread_number):
            i_from = i * interval
            i_to = len(data) if  i == (thread_number - 1) else i_from + interval

            thread = self.FW_Thread(i, data, i_from, i_to, self)
            thread.start()

            threads.append(thread)

        for t in threads :
            t.join()

        res = []
        od = OrderedDict(sorted(self.floyd_warshall_result.items()))
        for k, v in od.iteritems():
            res.append(v)

        # print 'self.maximal', self.maximal

        return res, max(self.max_list)

    class CompareThread(threading.Thread):
        def __init__(self, thread_ID, data1, data2, i_from, i_to, j_from, j_to, sp):
            threading.Thread.__init__(self)

            self.threadID = thread_ID
            self.sp = sp

            self.data1 = data1
            self.data2 = data2

            self.i_from = i_from
            self.i_to = i_to

            self.j_from = j_from
            self.j_to = j_to

        def run(self):
            for i in range(self.i_from, self.i_to) :
                for j in range(self.j_from, self.j_to) :
                    self.sp._calculate(self.data1[i], self.data2[j], i, j)

    def _calculate(self, graph1, graph2, i, j):
        if self.is_training and (self.res[j, i] > 0 or self.res[i, j] > 0) :
            return

        compared = np.array(graph1) == np.array(graph2)
        self.res[i, j] = np.sum(compared)

        if self.is_training:
            self.res[j, i] = self.res[i, j]

        # print 'Kernel: ', i, ', ', j, self.res[i, j]

    def _compute(self, data_1, data_2):

        self.is_training = False
        if data_1 is data_2:
            self.is_training = True

        self.res = np.zeros((len(data_1), len(data_2)))

        data_1 = basic.graphs_to_adjacency_lists(data_1)
        data_2 = data_1 if self.is_training else basic.graphs_to_adjacency_lists(data_2)

        data_1, max1 = self._apply_floyd_warshall(np.array(data_1))
        data_2, max2 = (data_1, max1) if self.is_training else self._apply_floyd_warshall(np.array(data_2))

        divided = 20
        jump_step_i = int(len(data_1) / divided)
        jump_step_j = int(len(data_2) / divided)

        threads = []

        for i in range(0, divided):
            for j in range(0, divided):
                i_from = i * jump_step_i
                j_from = j * jump_step_j
                i_to = len(data_1) if i == (divided - 1) else i_from + jump_step_i
                j_to = len(data_2) if j == (divided - 1) else j_from + jump_step_j

                thread = self.CompareThread(i + j, data_1, data_2, i_from, i_to, j_from, j_to, self)
                thread.start()

                threads.append(thread)

        for t in threads:
            t.join()

        return self.res

    def dim(self):
        return None
