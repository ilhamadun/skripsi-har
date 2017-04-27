#!/usr/bin/env python3

"""Normalize datasets

We use four public dataset, each of them has different formats.
This script is used to normalize all dataset to a common format.

How to use:
    ./normalize.py dataset_type path/to/dataset/directory path/to/output/directory

The dataset_type is one of these:
    - shoaib_2013: http://ps.cs.utwente.nl/Blog/Activity_Recognition_DataSet_Shoaib.rar
    - shoaib_2014: http://ps.cs.utwente.nl/Blog/Sensors_Activity_Recognition_DataSet_Shoaib.rar
    - mobiact: https://www.dropbox.com/s/sp8hrmrc2g2cy0u/MobiAct_Dataset.zip?dl=0
    - uci_har: https://archive.ics.uci.edu/ml/datasets/Human+Activity+Recognition+Using+Smartphones

"""

import csv
import fnmatch
import os
import fire
import numpy as np
from scipy import signal

def median_filter(features):
    """Filter each features with median filter"""
    filtered = []
    features = np.transpose(features)
    for feature in features:
        filtered.append(np.reshape(signal.medfilt(feature, 3), (-1, 1)))

    return np.concatenate(filtered, axis=1)

class Normalize:
    """Normalize datasets to common format

    Format specifiaction:
        - First line: [number of sample, number of features]
        - Remaining lines: [acc_x, acc_y, acc_z, gyro_x, gyro_y, gyro_z, label]
        - All features are filtered with median filter

    """
    def __init__(self):
        os.makedirs('sample', exist_ok=True)

    def shoaib_2013(self, input_path, output_path):
        """Physical Activity Recognition Dataset Using Smartphone Sensors

        The datasets are excel files, please convert it to csv first before normalizing.

        Shoaib, M. and Scholten, J. and Havinga, P.J.M.
        (2013) Towards physical activity recognition using smartphone sensors.
        In: 10th IEEE International Conference on Ubiquitous Intelligence and Computing, UIC 2013,
        18-20 Dec 2013, Vietri sul Mare, Italy. pp. 80-87. IEEE Computer Society.

        http://ps.cs.utwente.nl/Blog/Activity_Recognition_DataSet_Shoaib.rar
        """
        def _filter(input_path, output_path):
            read = []
            print('Opening datasets:', input_path)

            with open(input_path, 'r') as s:
                reader = csv.reader(s)

                for i, row in enumerate(reader):
                    if i < 1:
                        continue
                    read.append(row[1:7] + [_class_id(row[10])])


            dataset = np.array(read, dtype=np.float64)
            filtered_features = median_filter(dataset[:, :6])
            dataset = np.concatenate([filtered_features, dataset[:, 6:7]], axis=1)
            header = str(dataset.shape[0]) + ',6'
            np.savetxt(output_path, dataset, delimiter=',', header=header, comments='')
            print('Output:', output_path)

        def _class_id(activity_class):
            ret = -1
            if activity_class == 'Standing':
                ret = 1
            elif activity_class == 'Sitting':
                ret = 2
            elif activity_class == 'Walking':
                ret = 3
            elif activity_class == 'Running':
                ret = 4
            elif activity_class == 'Upstairs':
                ret = 5
            elif activity_class == 'Downstairs':
                ret = 6

            return ret

        os.makedirs(os.path.join(output_path, 'shoaib_2013'), exist_ok=True)
        _filter(os.path.join(input_path, 'Pocket.csv'),
                os.path.join(output_path, 'shoaib_2013', 'pocket.csv'))
        _filter(os.path.join(input_path, 'Belt.csv'),
                os.path.join(output_path, 'shoaib_2013', 'belt.csv'))

    def shoaib_2014(self, input_path, output_path):
        """Sensors activity dataset

        Shoaib, M. and Bosch, S. and Incel, O.D. and Scholten, H. and Havinga, P.J.M.
        (2014) Fusion of Smartphone Motion Sensors for Physical Activity Recognition.
        Sensors, 14, 10146-10176

        http://ps.cs.utwente.nl/Blog/Sensors_Activity_Recognition_DataSet_Shoaib.rar

        """
        def _filter(input_path, output_path):
            read = []

            print('Opening datasets:', input_path)
            with open(input_path, 'r') as s:
                reader = csv.reader(s)

                for i, row in enumerate(reader):
                    if i < 2:
                        continue
                    read.append(row[1:4] + row[7:10] + [_class_id(row[69])])
                    read.append(row[15:18] + row[21:24] + [_class_id(row[69])])
                    read.append(row[57:60] + row[63:66] + [_class_id(row[69])])

            dataset = np.array(read, dtype=np.float64)
            filtered_features = median_filter(dataset[:, :6])
            dataset = np.concatenate([filtered_features, dataset[:, 6:7]], axis=1)
            header = str(dataset.shape[0]) + ',6'
            np.savetxt(output_path, dataset, delimiter=',', header=header, comments='')
            print('Output:', output_path)

        def _class_id(activity_class):
            ret = -1
            if activity_class == 'standing':
                ret = 1
            elif activity_class == 'sitting':
                ret = 2
            elif activity_class == 'walking':
                ret = 3
            elif activity_class == 'jogging':
                ret = 4
            elif activity_class == 'upstairs':
                ret = 5
            elif activity_class == 'downstairs':
                ret = 6
            elif activity_class == 'biking':
                ret = 7

            return ret

        os.makedirs(os.path.join(output_path, 'shoaib_2014'), exist_ok=True)
        input_path = os.path.join(input_path, 'Participant_')
        output_path = os.path.join(output_path, 'shoaib_2014', 'activity_')
        for i in range(1, 11):
            _filter(input_path + str(i) + '.csv', output_path + str(i) + '.csv')

    def mobiact(self, input_path, output_path):
        """MobiAct Datasets

        Vavoulas, G., Chatzaki, C., Malliotakis, T., Pediaditis, M. and Tsiknakis, M.,
        The MobiAct Dataset: Recognition of Activities of Daily Living using Smartphones.,
        In Proceedings of the International Conference on Information and
        Communication Technologies for Ageing Well and e-Health (ICT4AWE 2016), pages 143-151

        https://www.dropbox.com/s/sp8hrmrc2g2cy0u/MobiAct_Dataset.zip?dl=0

        """
        def _filter(activity, acc_path, gyro_path, output_path):
            print('Opening datasets: [', acc_path, ', ', gyro_path, ']', sep='')
            raw_acc = np.loadtxt(acc_path, delimiter=',', skiprows=16)
            raw_gyr = np.loadtxt(gyro_path, delimiter=',', skiprows=16)

            num_sample = _class_num_sample(activity)
            resampled_acc = signal.resample(raw_acc[:, 1:4], num_sample)
            resampled_gyro = signal.resample(raw_gyr[:, 1:4], num_sample)
            filtered_acc = median_filter(resampled_acc)
            filtered_gyro = median_filter(resampled_gyro)
            activity_id = np.full((num_sample, 1), _class_id(activity))

            dataset = np.concatenate([filtered_acc, filtered_gyro, activity_id], 1)

            header = str(num_sample) + ',6'
            np.savetxt(output_path, dataset, delimiter=',', header=header, comments='')
            print('Saved to:', output_path)

        def _class_id(activity_class):
            ret = -1
            if activity_class == 'STD':
                ret = 1
            elif activity_class == 'SCH':
                ret = 2
            elif activity_class == 'WAL':
                ret = 3
            elif activity_class == 'JOG':
                ret = 4
            elif activity_class == 'STU':
                ret = 5
            elif activity_class == 'STN':
                ret = 6

            return ret

        def _class_num_sample(activity_class):
            if activity_class == 'STD':
                ret = 5 * 60 * 50
            elif activity_class == 'SCH':
                ret = 6 * 50
            elif activity_class == 'WAL':
                ret = 5 * 60 * 50
            elif activity_class == 'JOG':
                ret = 30 * 50
            elif activity_class == 'STU':
                ret = 10 * 50
            elif activity_class == 'STN':
                ret = 10 * 50

            return ret

        def _filter_activity(activity, input_path, output_path):
            std_list = os.listdir(os.path.join(input_path, activity))
            std_acc_list = fnmatch.filter(std_list, activity + '_acc*')
            std_gyro_list = fnmatch.filter(std_list, activity + '_gyro*')
            std_acc_list.sort()
            std_gyro_list.sort()
            activity_dir = os.path.join(output_path, 'mobiact', activity)
            os.makedirs(activity_dir, exist_ok=True)

            for i, _ in enumerate(std_acc_list):
                input_acc = os.path.join(input_path, activity, std_acc_list[i])
                input_gyro = os.path.join(input_path, activity, std_gyro_list[i])
                output = os.path.join(activity_dir,
                                      std_acc_list[i][:4] + std_acc_list[i][8:-4] + '.csv')
                _filter(activity, input_acc, input_gyro, output)

        _filter_activity('STD', input_path, output_path)
        _filter_activity('SCH', input_path, output_path)
        _filter_activity('WAL', input_path, output_path)
        _filter_activity('JOG', input_path, output_path)
        _filter_activity('STU', input_path, output_path)
        _filter_activity('STN', input_path, output_path)


    def uci_har(self, input_path, output_path):
        """Human Activity Recognition Using Smartphones Data Set

        Davide Anguita, Alessandro Ghio, Luca Oneto, Xavier Parra and Jorge L. Reyes-Ortiz.
        A Public Domain Dataset for Human Activity Recognition Using Smartphones.
        21th European Symposium on Artificial Neural Networks,
        Computational Intelligence and Machine Learning, ESANN 2013.
        Bruges, Belgium 24-26 April 2013.

        https://archive.ics.uci.edu/ml/datasets/Human+Activity+Recognition+Using+Smartphones

        """
        def _reverse_sliding_window(data):
            sliced = data[:, 0:64]

            return np.reshape(sliced, (sliced.shape[0] * sliced.shape[1], 1))

        def _class_id(activity_class):
            ret = -1
            if activity_class == 5:
                ret = 1
            elif activity_class == 4:
                ret = 2
            elif activity_class == 1:
                ret = 3
            elif activity_class == 2:
                ret = 4
            elif activity_class == 3:
                ret = 5
            elif activity_class == 6:
                ret = 8

            return ret

        def _filter(data_type, input_path, output_path):
            inertial_path = os.path.join(input_path, 'UCI HAR Dataset', data_type,
                                         'Inertial Signals')
            sensor_data = [None] * 6

            for i, axis in enumerate(['x', 'y', 'z']):
                acc_path = os.path.join(inertial_path,
                                        'total_acc_' + axis + '_' + data_type + '.txt')
                gyro_path = os.path.join(inertial_path,
                                         'body_gyro_' + axis + '_' + data_type + '.txt')

                sensor_data[i] = _reverse_sliding_window(np.loadtxt(acc_path)) * 9.80665
                sensor_data[i + 3] = _reverse_sliding_window(np.loadtxt(gyro_path))

            file_labels = np.loadtxt(os.path.join(input_path, 'UCI HAR Dataset', data_type,
                                                  'y_' + data_type + '.txt'))
            labels = np.empty((sensor_data[0].shape[0], 1))

            for i, label in enumerate(file_labels):
                labels[i*64:(i*64)+64] = _class_id(label)

            dataset = np.concatenate(sensor_data + [labels], axis=1)

            output_path = os.path.join(output_path, 'uci_har')

            header = str(dataset.shape[0]) + ',6'
            np.savetxt(os.path.join(output_path, data_type + '.csv'), dataset, delimiter=',',
                       header=header, comments='')

        os.makedirs(output_path, exist_ok=True)
        _filter('train', input_path, output_path)
        _filter('test', input_path, output_path)


if __name__ == '__main__':
    fire.Fire(Normalize)
