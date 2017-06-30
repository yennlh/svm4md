"""
A module containing Shortest Path Kernel.
"""
__author__ = 'kasiajanocha'

import numpy as np
import numpy.matlib as matlib

import basic
import threading

from collections import OrderedDict
from pykernels.base import Kernel, GraphKernel
from scipy.sparse import lil_matrix
from scipy.sparse.csgraph import floyd_warshall


class ShortestPath(GraphKernel):
    """
    Shortest Path kernel [3]
    """

    def __init__(self, labeled=False):
        self.labeled = labeled

    class MyThread(threading.Thread):
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
        # N = adj_mat.shape[0]
        # res = np.zeros((N, N))
        # res = res + ((adj_mat != 0) * weights)
        # res[res == 0] = np.inf
        # np.fill_diagonal(res, 0)
        # for i in xrange(N):
        #     for j in xrange(N):
        #         for k in xrange(N):
        #             if res[i, j] + res[j, k] < res[i, k]:
        #                 res[i, k] = res[i, j] + res[j, k]

        res = floyd_warshall(lil_matrix(adj_mat))

        self.floyd_warshall_result[index] = res
        self.max_list[index] = (res[~np.isinf(res)]).max()

        print 'Finish FW: ', index

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

            thread = self.MyThread(i, data, i_from, i_to, self)
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

    def _create_accum_list(self, shortest_paths, maxpath):
        """
        Construct accumulation array matrix for one dataset
        containing unlabaled graph data.
        """
        res = lil_matrix(np.zeros((len(shortest_paths), int(maxpath + 1))))
        for i, s in enumerate(shortest_paths):
            subsetter = (~(np.isinf(s)))
            # subsetter = np.triu(~(np.isinf(s)))
            ind = s[subsetter]
            accum = np.zeros(int(maxpath + 1))
            accum[:int(ind.max() + 1)] += np.bincount(ind.astype(int))
            res[i] = lil_matrix(accum)
        return res

    def _compute(self, data_1, data_2):

        self.is_training = False
        if data_1 is data_2:
            self.is_training = True

        data_1 = basic.graphs_to_adjacency_lists(data_1)
        data_2 = data_1 if self.is_training else basic.graphs_to_adjacency_lists(data_2)

        sp_1, max1 = self._apply_floyd_warshall(np.array(data_1))
        sp_2, max2 = (sp_1, max1) if self.is_training else self._apply_floyd_warshall(np.array(data_2))

        max_path = max(max1, max2)

        accum_list_1 = self._create_accum_list(sp_1, max_path)
        accum_list_2 = accum_list_1 if self.is_training else self._create_accum_list(sp_2, max_path)

        return np.asarray(accum_list_1.dot(accum_list_2.T).todense())

    def dim(self):
        return None
