"""Plot sensor data

Create plot for each of accelerometer and gyroscop axis

How to use:
    `python plot.py /path/to/sample /path/to/output/image`

"""

import fire
import matplotlib.pyplot as plt
import numpy as np

def plot(sample, output):
    """Plot sensor data

    Args:
        - `sample`: path to sensor data
        - `output`: path to output image

    """
    data = np.loadtxt(sample, delimiter=',', skiprows=1)
    data = data[100:300, :6]
    ax, ay, az, gx, gy, gz = np.hsplit(data, 6)

    f, axarr = plt.subplots(2, 3)
    plt.subplots_adjust(wspace=0.3, hspace=0.35)
    axarr[0, 0].plot(ax)
    axarr[0, 0].set_title('Ax')
    axarr[0, 0].set_ylim(-20, 20)
    axarr[0, 1].plot(ay)
    axarr[0, 1].set_title('Ay')
    axarr[0, 1].set_ylim(-20, 20)
    axarr[0, 2].plot(az)
    axarr[0, 2].set_title('Az')
    axarr[0, 2].set_ylim(-20, 20)

    axarr[1, 0].plot(gx)
    axarr[1, 0].set_title('Gx')
    axarr[1, 0].set_ylim(-20, 20)
    axarr[1, 1].plot(gy)
    axarr[1, 1].set_title('Gy')
    axarr[1, 1].set_ylim(-20, 20)
    axarr[1, 2].plot(gz)
    axarr[1, 2].set_title('Gz')
    axarr[1, 2].set_ylim(-20, 20)

    plt.savefig(output, dpi=250, bbox_inches='tight', pad_inches=0.1, format='png')
    print('Activity plot saved to ' + output)

if __name__ == '__main__':
    fire.Fire(plot)
