import pandas as pd

# Leer archivo .txt
archivo = 'output/data.txt'
df = pd.read_csv(archivo)

print(df)