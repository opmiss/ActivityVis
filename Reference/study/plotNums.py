"""
Demo of scatter plot with varying marker colors and sizes.
"""
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.cbook as cbook
import random
import csv

num=783
D = np.zeros((num, 3))
"""
# generate random data

for i in range(num):
    D[i,0] = np.floor(random.uniform(1, 1000)); 
    D[i,1] = np.floor(random.uniform(1, 100)); 
    
# save random data

with open('random.csv', 'wb') as csvfile:
    writer = csv.writer(csvfile, delimiter=',')
    for i in range(num):
        writer.writerow(D[i,:])
"""        
# read random data
k=0
with open('nums.csv', 'rU') as f:
    reader = csv.reader(f)
    for row in reader:
        D[k, 0] = row[0]
        D[k, 1] = row[1]
        D[k, 2] = row[2]
        k = k+1
m,b = np.polyfit(D[:,0], D[:,1], 1) 
print m,b
def f(t):
    return m*t+b
x = np.arange(1200); 

fig, ax = plt.subplots()
ax.scatter(D[:,0], D[:,1], c='b')
plt.plot(x, f(x), 'r--')
#ax.scatter(D[:,0], D[:,2], c='r')

ax.set_xlabel(r'number of contacts', fontsize=18)
ax.set_ylabel(r'number of emails sent', fontsize=18)
plt.ylim([0, 10000])
plt.xlim([0, 1200])
#ax.set_title('')

ax.grid(True)
fig.tight_layout()
plt.show()
