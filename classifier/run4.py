from sklearn.externals import joblib
from sklearn.svm import SVC
from sklearn.metrics import accuracy_score
from pykernels.graph.shortestpath2 import ShortestPath
from config import ConfigSVM

import sys
import numpy as np
import timeit
import datetime

from os import listdir
from os.path import isfile, join


def read_file(path):
    sparse_matrices = np.array(np.genfromtxt(path, delimiter=','))
    res = np.zeros((ConfigSVM.matrix_size, ConfigSVM.matrix_size))

    try:
        if (len(sparse_matrices.shape) == 2):
            for m in sparse_matrices:
                res[int(m[0]) - 1, int(m[1]) - 1] = int(m[2])
        else:
            res[int(sparse_matrices[0]) - 1, int(sparse_matrices[1]) - 1] = sparse_matrices[0]
    except Exception, ex:
        print ex.message
        print "Error file: ", path
        print m, sparse_matrices
        raise ex
    print  path
    return res


CFG = './CFG_out/CFG_out_798_2156'

if (len(sys.argv) == 3):
    CFG = './AST/' + sys.argv[1]
    ConfigSVM.matrix_size = int(sys.argv[2])

    train_mal_path = CFG + '/training/a/'
    train_non_path = CFG + '/training/b/'
    test_mal_path = CFG + '/testing/a/'
    test_non_path = CFG + '/testing/b/'

    train_mal_files = [train_mal_path + f for f in listdir(train_mal_path) if isfile(join(train_mal_path, f))]
    train_non_files = [train_non_path + f for f in listdir(train_non_path) if isfile(join(train_non_path, f))]
    test_mal_files = [test_mal_path + f for f in listdir(test_mal_path) if isfile(join(test_mal_path, f))]
    test_non_files = [test_non_path + f for f in listdir(test_non_path) if isfile(join(test_non_path, f))]

if (len(sys.argv) == 4):
    CFG = './5Folds/' + sys.argv[1]
    ConfigSVM.matrix_size = int(sys.argv[2])
    test_fold = int(sys.argv[3])

    print 'Testing fold: ', test_fold

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
# else:
    # train_mal_path = CFG + '/training/malware/'
    # train_non_path = CFG + '/training/non-malware/'
    # test_mal_path = CFG + '/testing/malware/'
    # test_non_path = CFG + '/testing/non-malware/'

    # train_mal_files = [train_mal_path + f for f in listdir(train_mal_path) if isfile(join(train_mal_path, f))]
    # train_non_files = [train_non_path + f for f in listdir(train_non_path) if isfile(join(train_non_path, f))]
    # test_mal_files = [test_mal_path + f for f in listdir(test_mal_path) if isfile(join(test_mal_path, f))]
    # test_non_files = [test_non_path + f for f in listdir(test_non_path) if isfile(join(test_non_path, f))]

print 'Matrix Size: ', ConfigSVM.matrix_size
print 'Finished to load paths'

##############################################

train_data = []

for p in train_mal_files:
    train_data.append(read_file(p).ravel())

for p in train_non_files:
    train_data.append(read_file(p).ravel())

# train_data = [[1, 1, 1, 0, 0, 0, 0, 0, 0],
#               [0, 0, 1, 1, 0, 0, 0, 0, 0],
#               [0, 0, 1, 1, 1, 0, 0, 0, 0],
#               [0, 0, 0, 0, 0, 0, 0, 1, 1],
#               [0, 0, 0, 0, 0, 0, 1, 1, 1]
#               ]

train_data = np.array(train_data)

print 'Finished to load training data'

##############################################

# print ShortestPath().gram(train_data)

##############################################

test_data = []

for p in test_mal_files:
    test_data.append(read_file(p).ravel())

for p in test_non_files:
    test_data.append(read_file(p).ravel())

# test_data = [[1, 1, 1, 0, 0, 0, 0, 0, 0],
#               [0, 0, 1, 1, 0, 0, 0, 0, 0],
#               [0, 0, 0, 0, 0, 0, 1, 1, 1]]

test_data = np.array(test_data)

print 'Finished to load testing data'

start = timeit.default_timer()

##############################################

# y = np.concatenate((np.ones(3), np.zeros(2)))
y = np.concatenate((np.ones(len(train_mal_files)), np.zeros(len(train_non_files))))

classifier = SVC(kernel=ShortestPath())
classifier.fit(train_data, y)

print 'Finished to feed data'

stop = timeit.default_timer()
training_time = stop - start
print 'Training Time: ', training_time
start = timeit.default_timer()

##############################################

# clf_file_name = './classifiers/run4-' + str(ConfigSVM.matrix_size) + '-' + str(datetime.datetime.now()) + '.joblib.pkl'
# joblib.dump(classifier, clf_file_name, compress=9)
# print 'Finished to store classifier'

predicted = classifier.predict(test_data)

print 'Predictions:', predicted
print 'Accuracy:', accuracy_score(predicted,
                                  np.concatenate((np.ones(len(test_mal_files)), np.zeros(len(test_non_files)))))

stop = timeit.default_timer()
print 'Training Time: ', training_time
print 'Testing Time: ', stop - start
