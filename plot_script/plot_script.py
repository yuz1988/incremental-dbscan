import random
import numpy as np
import matplotlib.pyplot as plt
from matplotlib import rc

plt.rcParams['axes.labelsize'] = 18
plt.rcParams['xtick.labelsize'] = 18
plt.rcParams['ytick.labelsize'] = 18
plt.rcParams['legend.fontsize'] = 18
rc('text', usetex=True)
rc('font', family='serif')


batch_fn = "batch-time.txt"
inc_fn = "inc-time.txt"
batch_data, inc_data = [], []

with open(batch_fn, "r") as batch, open(inc_fn, "r") as inc:
    for line in batch:
        batch_data.append(float(line))
    for line in inc:
        inc_data.append(float(line))

step = 100
iter_batch = np.arange(0, step*len(batch_data), step)
iter_inc = np.arange(0, step*len(inc_data), step)

plt.figure(figsize=(8, 6))
plt.plot(iter_batch, batch_data)
plt.plot(iter_inc, inc_data)
plt.xlabel('Number of points received')
plt.ylabel('Time Elapsed')
plt.legend(['Batch', 'Incremental'])
plt.savefig('time-result.png')

