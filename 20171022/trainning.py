import tensorflow as tf
from sklearn import datasets
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.utils import shuffle
import matplotlib.pyplot as plt
import os
class EarlyStopping:
    def __init__(self, patience=0, verbose=0):
        self._step = 0
        self._loss = float('inf')
        self.patience = patience
        self.verbose = verbose
    
    def validate(self, loss):
        if self._loss < loss:
            self._step += 1
            if self._step > self.patience:
                if self.verbose:
                    print('early stopping')
                return True
        else:
            self._stop = 0
            self._loss = loss
        
        return False

class DNN(object):
    def __init__(self, n_in, n_hiddens, n_out):
        # initialize
        self.n_in = n_in
        self.n_hiddens = n_hiddens
        self.n_out = n_out
        self.weights = []
        self.biasses = []
        self._x = None
        self._t = None
        self._keep_prob = None
        self._sess = None
        self._history = {
            'accuracy':[],
            'loss':[]
        }
        self._accuracy = None
    
    def weight_variable(self, shape, index):
        initial = tf.truncated_normal(shape, stddev=0.01)
        name='w'+str(index)
        print("w = " + name)
        return tf.Variable(initial, name)
    
    def bias_variable(self, shape, index):
        initial = tf.zeros(shape)
        name='b'+str(index)
        print("b = " + name)
        return tf.Variable(initial, name)
    
    def inference(self, x, keep_prob):
        # define model
        for i, n_hidden in enumerate(self.n_hiddens):
            if i == 0:
                input = x
                input_dim = self.n_in
            else:
                input = output
                input_dim = self.n_hiddens[i-1]
            
            self.weights.append(self.weight_variable([input_dim, n_hidden], i))
            self.biasses.append(self.bias_variable([n_hidden], i))
            
            h = tf.nn.relu(tf.matmul(
                input, self.weights[-1]) + self.biasses[-1])
            output = tf.nn.dropout(h, keep_prob)
      
        self.weights.append(self.weight_variable([self.n_hiddens[-1], self.n_out], len(self.n_hiddens)))
        self.biasses.append(self.bias_variable([self.n_out], len(self.n_hiddens)))
        
        y = tf.nn.softmax(tf.matmul(
            output, self.weights[-1]) + self.biasses[-1])       
        return y
    
    def loss(self, y, t):
        cross_entropy = tf.reduce_mean(-tf.reduce_sum(t * tf.log(tf.clip_by_value(y, 1e-10, 1.0)),
                                                     reduction_indices=[1]))
        return cross_entropy
    
    def training(self, loss):
        #optimizer = tf.train.GradientDescentOptimizer(0.01)
        optimizer = tf.train.AdamOptimizer(learning_rate=0.001, beta1=0.9, beta2=0.999)
        train_step = optimizer.minimize(loss)
        return train_step
    
    def accuracy(self, y, t):
        correct_prediction = tf.equal(tf.argmax(y, 1), tf.argmax(t, 1))
        accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))
        return accuracy
        
    def fit(self, X_train, Y_train, epochs=100, batch_size=100, p_keep=0.5, verbose=1):
        x = tf.placeholder(tf.float32, shape=[None, self.n_in])
        t = tf.placeholder(tf.float32, shape=[None, self.n_out])
        keep_prob = tf.placeholder(tf.float32)
        
        self._x = x
        self._t = t
        self._keep_prob = keep_prob
        
        y = self.inference(x, keep_prob)
        loss = self.loss(y, t)
        train_step = self.training(loss)
        self._accuracy = self.accuracy(y, t)
        
        init = tf.global_variables_initializer()
        saver = tf.train.Saver()
        sess = tf.Session()
        sess.run(init)
        
        self._sess = sess
        
        N_train = len(X_train)
        n_batches = N_train // batch_size
        
        early_stopping = EarlyStopping(patience=10, verbose=1)
        
        for epoch in range(epochs):
            X_,Y_ = shuffle(X_train, Y_train)
            
            for i in range(n_batches):
                start = i * batch_size
                end = start + batch_size
                
                sess.run(train_step, feed_dict={
                    x:X_[start:end],
                    t:Y_[start:end],
                    keep_prob:p_keep
                })
            loss_ = loss.eval(session=sess, feed_dict={
                x:X_train,
                t:Y_train,
                keep_prob:1.0
            })
            
            accuracy_ = self._accuracy.eval(session=sess, feed_dict={
                x:X_train,
                t:Y_train,
                keep_prob:1.0
            })
            
            self._history['loss'].append(loss_)
            self._history['accuracy'].append(accuracy_)
            
            if early_stopping.validate(loss_):
                break

            if verbose:
                print('epoch', epoch,
                     ' loss', loss_,
                     ' accuracy', accuracy_)
        model_path = saver.save(sess, MODEL_DIR + '/model.ckpt')
        print('Model saved to :', model_path)
        return self._history
        # process for learning
        
    def evaluate(self, X_test, Y_test):
        
        return self._accuracy.eval(session=self._sess, feed_dict={
            self._x: X_test,
            self._t: Y_test,
            self._keep_prob: 1.0
        })
    
    def plot_result(self, epochs=100):
        plt.rc('font', family='serif')
        fig = plt.figure()
        ax_acc = fig.add_subplot(111)
        ax_acc.plot(range(epochs), self._history['accuracy'], label='accuracy', color='black')
        
        ax_loss = ax_acc.twinx()
        ax_loss.plot(range(epochs), self._history['loss'], label='loss', color='gray')
        
        plt.xlabel('epochs')
        plt.ylabel('validation accuracy and loss')
        
        plt.show()
    
if __name__ == '__main__':
    #モデルの保存
    MODEL_DIR = os.path.join(os.path.dirname("__file__"), 'model_100')
    if os.path.exists(MODEL_DIR) is False:
        os.mkdir(MODEL_DIR)
    
    OUTPUT_LAYER = 4
    #ラベルファイルを読み込んで1-of-K表現にする
    y = np.loadtxt("./emo_label",delimiter=",")
    Y = np.eye(OUTPUT_LAYER)[y.astype(int)]
    #データファイルを読み込んで正規化する
    X = np.loadtxt("./emo_data",delimiter=",")
    X_train, X_test, Y_train, Y_test = train_test_split(X, Y)

    model = DNN(n_in=1000,
           n_hiddens=[100, 100, 100],
           n_out=OUTPUT_LAYER)
    history = model.fit(X_train, Y_train,
         epochs=100,
         batch_size=70,
         p_keep=0.7)
    accuracy = model.evaluate(X_test, Y_test)
    print('accuracy: ', accuracy)
    
    model.plot_result(len(history['loss']))
        # process for evaluatio

