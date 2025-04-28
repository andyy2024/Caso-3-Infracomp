import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

# Leer archivo .txt
archivo = 'output/data.txt'
df = pd.read_csv(archivo)
print(df.columns)  

firma = df[df['accion'] == 'firma']

promedio_firma_ns = firma['tiempo_ns'].mean()

cifrado_sim_df = df[df['accion'] == 'cifrar_tabla_(simetrico)']

cifrado_asim_df = df[df['accion'] == 'cifrar_tabla_(asimetrico)']
cifrado_sim_df = cifrado_sim_df[cifrado_sim_df['accion'] == 'cifrar_tabla_(simetrico)']

verificacion_df = df[df['accion'] == 'verificacion']

promedio_cifrado_ns = cifrado_sim_df['tiempo_ns'].mean()
promedio_verificacion_ns = verificacion_df['tiempo_ns'].mean()

promedio_cifrado_tabla_simetrico = cifrado_sim_df['tiempo_ns'].mean()
promedio_cifrado_tabla_asimetrico = cifrado_asim_df['tiempo_ns'].mean()





print(f"Tiempo promedio de firma (ns): {promedio_firma_ns}")

print(f"Tiempo promedio de cifrado simetrico (ns): {promedio_cifrado_ns}")

print(f"Tiempo promedio de verificacion (ns): {promedio_verificacion_ns}")

print(f"Tiempo promedio de cifrado tabla simetrico (ns): {promedio_cifrado_tabla_simetrico}")
print(f"Tiempo promedio de cifrado tabla asimetrico (ns): {promedio_cifrado_tabla_asimetrico}")



#print(df.head())
 
escenario_actual = 'Iterativo'



plt.figure(figsize=(10,6))
sns.scatterplot(x=firma.index, y=firma['tiempo_ns'], label='Firmas')
plt.axhline(firma['tiempo_ns'].mean(), color='red', linestyle='--', label=f'Media: {firma["tiempo_ns"].mean():.0f} ns')
plt.title(f'Tiempos individuales de Firma - {escenario_actual}')
plt.xlabel('Mediciones')
plt.ylabel('Tiempo de firma (ns)')
plt.yscale('log')
plt.legend()
plt.grid(True)
plt.show()

plt.figure(figsize=(10,6))
sns.scatterplot(x=cifrado_sim_df.index, y=cifrado_sim_df['tiempo_ns'], label='Cifrado Simétrico (AES)')
plt.axhline(cifrado_sim_df['tiempo_ns'].mean(), color='red', linestyle='--', label=f'Media: {cifrado_sim_df["tiempo_ns"].mean():.0f} ns')
plt.title(f'Tiempos individuales de Cifrado Simétrico - {escenario_actual}')
plt.xlabel('Mediciones')
plt.ylabel('Tiempo de cifrado simétrico (ns)')
plt.yscale('log')
plt.legend()
plt.grid(True)
plt.show()

plt.figure(figsize=(10,6))
sns.scatterplot(x=verificacion_df.index, y=verificacion_df['tiempo_ns'], label='Verificaciones')
plt.axhline(verificacion_df['tiempo_ns'].mean(), color='red', linestyle='--', label=f'Media: {verificacion_df["tiempo_ns"].mean():.0f} ns')
plt.title(f'Tiempos individuales de Verificación - {escenario_actual}')
plt.xlabel('Mediciones')
plt.ylabel('Tiempo de verificación (ns)')
plt.yscale('log')
plt.legend()
plt.grid(True)
plt.show()

plt.figure(figsize=(10,6))
sns.scatterplot(x=cifrado_sim_df.index, y=cifrado_sim_df['tiempo_ns'], label='Cifrado Simétrico (AES)')
sns.scatterplot(x=cifrado_asim_df.index, y=cifrado_asim_df['tiempo_ns'], label='Cifrado Asimétrico (RSA)')
plt.axhline(cifrado_sim_df['tiempo_ns'].mean(), color='blue', linestyle='--', label=f'Media Simétrico: {cifrado_sim_df["tiempo_ns"].mean():.0f} ns')
plt.axhline(cifrado_asim_df['tiempo_ns'].mean(), color='orange', linestyle='--', label=f'Media Asimétrico: {cifrado_asim_df["tiempo_ns"].mean():.0f} ns')
plt.title(f'Comparación Cifrado Simétrico vs Asimétrico - {escenario_actual}')
plt.xlabel('Mediciones')
plt.ylabel('Tiempo de cifrado (ns)')
plt.yscale('log')
plt.legend()
plt.grid(True)
plt.show()

plt.figure(figsize=(10,6))
sns.boxplot(x='accion', y='tiempo_ns', data=df)
plt.yscale('log')
plt.title(f'Distribución de tiempos por tipo de acción - {escenario_actual}')
plt.xlabel('Acción')
plt.ylabel('Tiempo (ns) (escala log)')
plt.grid(True, which="both", ls="--")
plt.show()