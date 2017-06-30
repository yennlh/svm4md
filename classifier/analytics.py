from sklearn.svm import SVC
from sklearn.metrics import accuracy_score
from pykernels.graph.randomwalk import RandomWalk
from pykernels.graph.shortestpath import ShortestPath
from pykernels.graph.randomwalk import _norm
from config import ConfigSVM

import sys
import numpy as np
import timeit

from os import listdir
from os.path import isfile, join

CFG = './5Folds/5Folds_out_args_new_1719'
ConfigSVM.matrix_size = 1719
test_fold = 1

test_mal_path = CFG + '/Fold' + str(test_fold) + '/malware/'
test_non_path = CFG + '/Fold' + str(test_fold) + '/non-malware/'
test_mal_files = [test_mal_path + f for f in listdir(test_mal_path) if isfile(join(test_mal_path, f))]
test_non_files = [test_non_path + f for f in listdir(test_non_path) if isfile(join(test_non_path, f))]

train_mal_files = []
train_non_files = []
for i in range(1, 6):
    if i == test_fold:
        continue

    train_mal_path = CFG + '/Fold' + str(i) + '/malware/'
    train_non_path = CFG + '/Fold' + str(i) + '/non-malware/'

    train_mal_files.extend([train_mal_path + f for f in listdir(train_mal_path) if isfile(join(train_mal_path, f))])
    train_non_files.extend([train_non_path + f for f in listdir(train_non_path) if isfile(join(train_non_path, f))])

data = np.genfromtxt('out-decision-5Folds_out_args_new_1719_F1.txt')

predict = data[0]
label = data[1]

for i in range(0, len(predict)) :
    if ((predict[i] > 0 and label[i] == 1) or (predict[i] < 0 and label[i] == 0)) :
        # print 'TRUE'
        continue
    else :
        # print 'FALSE'
        if (i >= len(test_mal_files)) :
            print  test_non_files[i - len(test_mal_files)]
        else :
            print test_mal_files[i]

# matrix1 = [[[0.0, 1.0, 0.0, 4.0],
#            [0.0, 0.0, 2.0, 0.0],
#            [3.0, 0.0, 0.0, 0.0],
#            [2.0, 0.0, 0.0, 0.0]]]
#
# matrix2 = [[[0.0, 1.0, 3.0, 0.0],
#            [0.0, 0.0, 2.0, 0.0],
#            [0.0, 0.0, 4.0, 0.0],
#            [2.0, 0.0, 0.0, 0.0]]]
#
# print RandomWalk()._compute(matrix1, matrix2)