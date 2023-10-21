import matplotlib.pyplot as plt

# data for comparison
topologies = ['Centralized', 'Decentralized']
mean_latency = [4.2, 3.8]  # Means
std_deviation_latency = [0.6, 0.4]  # Standard deviation

# data plot
fig, ax = plt.subplots()
ax.bar(topologies, mean_latency, yerr=std_deviation_latency, align='center', alpha=0.5, ecolor='black', capsize=10)
ax.set_ylabel('Latency')
ax.set_title('Comparison of Data Transfer Performance')
ax.yaxis.grid(True)

# Displaying the graph
plt.show()
