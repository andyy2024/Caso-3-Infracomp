import pandas as pd

archivo = 'output/data.txt'
df = pd.read_csv(archivo)
grouped = df.groupby('accion').mean()
print(grouped)