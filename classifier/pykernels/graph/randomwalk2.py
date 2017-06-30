"""
A module containing Random Walk Kernel.
"""

__author__ = 'kasiajanocha'

import numpy as np
from pykernels.base import Kernel, GraphKernel
from scipy.sparse import lil_matrix, kron,identity
from scipy.sparse.linalg import lsqr
import threading
import basic

def _norm(adj_mat):
    """Normalize adjacency matrix"""
    norm = adj_mat.sum(axis=0)
    norm[norm == 0] = 1
    return adj_mat / norm

class RandomWalk(GraphKernel):
    """
    Unlabeled random walk kernel [1]
    using conjugate gradient method 
    """

    def __init__(self, lmb=0.5, tolerance=1e-8, maxiter=20):
        self._lmb = lmb
        self._tolerance = tolerance
        self._max_iter = maxiter


    class MyThread(threading.Thread):
        def __init__(self, thread_ID, data1, data2, i_from, i_to, j_from, j_to, random_walk):
            threading.Thread.__init__(self)

            self.threadID = thread_ID
            self.random_walk = random_walk

            self.data1 = data1
            self.data2 = data2

            self.i_from = i_from
            self.i_to = i_to

            self.j_from = j_from
            self.j_to = j_to

        def run(self):
            for i in range(self.i_from, self.i_to) :
                for j in range(self.j_from, self.j_to) :
                    self.random_walk._calculate(self.data1[i], self.data2[j], i, j)


    def _calculate(self, graph1, graph2, i, j):
        if self.is_training and (self.res[j, i] > 0 or self.res[i, j] > 0) :
            return

        # norm1, norm2 - normalized adjacency matrixes
        # norm1 = _norm(graph1)
        # norm2 = _norm(graph2)
        norm1 = graph1
        norm2 = graph2

        # if graph is unweighted, W_prod = kron(a_norm(g1)*a_norm(g2))
        w_prod = kron(lil_matrix(norm1), lil_matrix(norm2))
        starting_prob = np.ones(w_prod.shape[0]) / (w_prod.shape[0])
        stop_prob = starting_prob

        # first solve (I - lambda * W_prod) * x = p_prod
        A = identity(w_prod.shape[0]) - (w_prod * self._lmb)
        x = lsqr(A, starting_prob)

        self.res[i, j] = stop_prob.T.dot(x[0])

        if self.is_training:
            self.res[j, i] = self.res[i, j]

        print 'Kernel: ', i, ', ', j, self.res[i, j]

    # either tensor of dimention 3 (list of adjacency matrices)
    def _compute(self, data_1, data_2):

        self.is_training = False
        if data_1 is data_2 :
            self.is_training = True

        data_1 = basic.graphs_to_adjacency_lists(data_1)
        data_2 = data_1 if self.is_training else basic.graphs_to_adjacency_lists(data_2)

        self.res = np.zeros((len(data_1), len(data_2)))

        # for i, graph1 in enumerate(data_1):
        #     for j, graph2 in enumerate(data_2):
        #         self._calculate(graph1, graph2, i, j)

        devided = 20
        jump_step_i = int(len(data_1) / devided)
        jump_step_j = int(len(data_2) / devided)

        threads = []

        for i in range(0, devided) :
            for j in range(0, devided) :
                i_from = i * jump_step_i
                j_from = j * jump_step_j
                i_to = len(data_1) if  i == (devided - 1) else i_from + jump_step_i
                j_to = len(data_2) if  j == (devided - 1) else j_from + jump_step_j

                thread = self.MyThread(i + j, data_1, data_2, i_from, i_to, j_from, j_to, self)
                thread.start()

                threads.append(thread)

        for t in threads :
            t.join()

        return self.res

    def dim(self):
        return None
