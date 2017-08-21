"""Create plot from training and validation log

How to use:
    `python plot-pelatihan.py /path/to/train/logs /path/to/test/logs /path/to/output/image`

"""

import fire
import matplotlib.pyplot as plt
import numpy as np

def extract(filename):
    """Rctract log file

    Args:
        - `filename`: log file to extract

    Returns:
        a list of `step` and a list `accuracy`

    """
    data = np.loadtxt(filename, delimiter=',', skiprows=1)
    step = data[:, 1:2]
    accuracy = data[:, 2:3]

    step = np.reshape(step, step.shape[0])
    accuracy = np.reshape(accuracy, accuracy.shape[0])

    return step, accuracy


def plot(train_log, test_log, output):
    """Create plot

    Args:
        - `train_log`:  path to training logs
        - `test_log`:   path to test logs
        - `output`:     path to output image
    """
    train_step, train_accuracy = extract(train_log)
    test_step, test_accuracy = extract(test_log)

    _, ax = plt.subplots(figsize=(12, 5), tight_layout=True)
    ax.plot(train_step, train_accuracy, label='Akurasi data latih')
    ax.plot(test_step, test_accuracy, label='Akurasi data validasi')

    ax.legend(loc='lower right')
    ax.grid(True)
    ax.set_xlabel('Iterasi')
    ax.set_ylabel('Akurasi')
    plt.savefig(output)

    print('Plot created on \'', output, '\'', sep='')


if __name__ == '__main__':
    fire.Fire(plot)
