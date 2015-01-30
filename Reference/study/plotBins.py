"""
Demo of scatter plot with varying marker colors and sizes.
"""
from __future__ import division
import numpy as np
import matplotlib.pyplot as plt
import matplotlib.cbook as cbook
import random
import csv


num_of_months_in_year = 12; 
num_of_days_in_month = 31; 
num_of_days_in_week = 7; 
num_of_hours_in_day = 24;  
B = [];    

# read random data
with open('bins.csv', 'rU') as f:
    reader = csv.reader(f)
    for row in reader:
        B.append(row); 
        
Y = np.zeros(num_of_months_in_year); 
M = np.zeros(num_of_days_in_month); 
W = np.zeros(num_of_days_in_week); 
D = np.zeros(num_of_hours_in_day); 

"""
k=0
for s in B[0]:
    print s; 
    if s!='':
        d = int(s); 
        if d!=0:
            Y[k] = d/10000; 
            k=k+1; 
"""
"""
k=0
for s in B[1]: 
    if s!='':
        d = int(s); 
        if d!=0:
            M[k] = d/12/1000; 
            k=k+1; 
"""
k=0
for s in B[2]: 
    if s!='':
        d = int(s); 
        if d!=0:
            D[k] = d/365/100; 
            k=k+1; 
            
"""
width = 0.5;
ind =  np.arange(num_of_months_in_year);
fig, ax = plt.subplots()
rects = ax.bar(ind+0.2, Y, width, color='b')

ax.set_xticks(ind+width)
ax.set_xticklabels(('Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'))
ax.set_ylabel(r'volumes of emails sent in a year (x10000)', fontsize=18)
"""

width = 0.5;
ind =  np.arange(num_of_hours_in_day);
fig, ax = plt.subplots()
rects = ax.bar(ind+0.2, D, width, color='b')
ax.set_xticks(ind+width)
ax.set_xticklabels(('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23'))
ax.set_ylabel(r'volumes of emails sent in a day (x100)', fontsize=18)
plt.xlim([0, 24])

plt.show()
