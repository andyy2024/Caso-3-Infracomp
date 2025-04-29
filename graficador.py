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
 
escenario_actual = 'Escenario iterativo'  



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




df_iterativo = pd.read_csv('output/iterativo.txt')
df_2_4 = pd.read_csv('output/2-4.txt')
df_2_16 = pd.read_csv('output/2-16.txt')
df_2_32 = pd.read_csv('output/2-32.txt')
df_2_64 = pd.read_csv('output/2-64.txt')

cifrado_sim_iterativo = df_iterativo[df_iterativo['accion'] == 'cifrar_tabla_(simetrico)']
cifrado_sim_2_4 = df_2_4[df_2_4['accion'] == 'cifrar_tabla_(simetrico)']
cifrado_sim_2_16 = df_2_16[df_2_16['accion'] == 'cifrar_tabla_(simetrico)']
cifrado_sim_2_32 = df_2_32[df_2_32['accion'] == 'cifrar_tabla_(simetrico)']
cifrado_sim_2_64 = df_2_64[df_2_64['accion'] == 'cifrar_tabla_(simetrico)']

verificacion_iterativo = df_iterativo[df_iterativo['accion'] == 'verificacion']
verificacion_2_4 = df_2_4[df_2_4['accion'] == 'verificacion']
verificacion_2_16 = df_2_16[df_2_16['accion'] == 'verificacion']
verificacion_2_32 = df_2_32[df_2_32['accion'] == 'verificacion']
verificacion_2_64 = df_2_64[df_2_64['accion'] == 'verificacion']


cifrado_sim_iterativo = df_iterativo[df_iterativo['accion'] == 'cifrar_tabla_(simetrico)']
cifrado_asim_iterativo = df_iterativo[df_iterativo['accion'] == 'cifrar_tabla_(asimetrico)']

cifrado_sim_2_4 = df_2_4[df_2_4['accion'] == 'cifrar_tabla_(simetrico)']
cifrado_asim_2_4 = df_2_4[df_2_4['accion'] == 'cifrar_tabla_(asimetrico)']

cifrado_sim_2_16 = df_2_16[df_2_16['accion'] == 'cifrar_tabla_(simetrico)']
cifrado_asim_2_16 = df_2_16[df_2_16['accion'] == 'cifrar_tabla_(asimetrico)']

cifrado_sim_2_32 = df_2_32[df_2_32['accion'] == 'cifrar_tabla_(simetrico)']
cifrado_asim_2_32 = df_2_32[df_2_32['accion'] == 'cifrar_tabla_(asimetrico)']

cifrado_sim_2_64 = df_2_64[df_2_64['accion'] == 'cifrar_tabla_(simetrico)']
cifrado_asim_2_64 = df_2_64[df_2_64['accion'] == 'cifrar_tabla_(asimetrico)']



plt.figure(figsize=(12,7))
sns.scatterplot(x=cifrado_sim_iterativo.index, y=cifrado_sim_iterativo['tiempo_ns'], label='Iterativo')
sns.scatterplot(x=cifrado_sim_2_4.index, y=cifrado_sim_2_4['tiempo_ns'], label='2-4 clientes')
sns.scatterplot(x=cifrado_sim_2_16.index, y=cifrado_sim_2_16['tiempo_ns'], label='2-16 clientes')
sns.scatterplot(x=cifrado_sim_2_32.index, y=cifrado_sim_2_32['tiempo_ns'], label='2-32 clientes')
sns.scatterplot(x=cifrado_sim_2_64.index, y=cifrado_sim_2_64['tiempo_ns'], label='2-64 clientes')

plt.title('Comparación de tiempos de Cifrado Simétrico entre escenarios')
plt.xlabel('Mediciones')
plt.ylabel('Tiempo de cifrado simétrico (ns)')
plt.yscale('log')
plt.grid(True)
plt.legend()
plt.show()


plt.figure(figsize=(12,7))
sns.scatterplot(x=verificacion_iterativo.index, y=verificacion_iterativo['tiempo_ns'], label='Iterativo')
sns.scatterplot(x=verificacion_2_4.index, y=verificacion_2_4['tiempo_ns'], label='2-4 clientes')
sns.scatterplot(x=verificacion_2_16.index, y=verificacion_2_16['tiempo_ns'], label='2-16 clientes')
sns.scatterplot(x=verificacion_2_32.index, y=verificacion_2_32['tiempo_ns'], label='2-32 clientes')
sns.scatterplot(x=verificacion_2_64.index, y=verificacion_2_64['tiempo_ns'], label='2-64 clientes')
plt.title('Comparación de tiempos de Verificación entre escenarios')
plt.xlabel('Mediciones')
plt.ylabel('Tiempo de verificación (ns)')
plt.yscale('log')
plt.grid(True)
plt.legend()
plt.show()



plt.figure(figsize=(12,7))

sns.scatterplot(x=cifrado_sim_iterativo.index, y=cifrado_sim_iterativo['tiempo_ns'], label='Simétrico - Iterativo')
sns.scatterplot(x=cifrado_asim_iterativo.index, y=cifrado_asim_iterativo['tiempo_ns'], label='Asimétrico - Iterativo')

sns.scatterplot(x=cifrado_sim_2_4.index, y=cifrado_sim_2_4['tiempo_ns'], label='Simétrico - 2-4 clientes')
sns.scatterplot(x=cifrado_asim_2_4.index, y=cifrado_asim_2_4['tiempo_ns'], label='Asimétrico - 2-4 clientes')

sns.scatterplot(x=cifrado_sim_2_16.index, y=cifrado_sim_2_16['tiempo_ns'], label='Simétrico - 2-16 clientes')
sns.scatterplot(x=cifrado_asim_2_16.index, y=cifrado_asim_2_16['tiempo_ns'], label='Asimétrico - 2-16 clientes')

sns.scatterplot(x=cifrado_sim_2_32.index, y=cifrado_sim_2_32['tiempo_ns'], label='Simétrico - 2-32 clientes')
sns.scatterplot(x=cifrado_asim_2_32.index, y=cifrado_asim_2_32['tiempo_ns'], label='Asimétrico - 2-32 clientes')

sns.scatterplot(x=cifrado_sim_2_64.index, y=cifrado_sim_2_64['tiempo_ns'], label='Simétrico - 2-64 clientes')
sns.scatterplot(x=cifrado_asim_2_64.index, y=cifrado_asim_2_64['tiempo_ns'], label='Asimétrico - 2-64 clientes')

plt.axhline(cifrado_sim_iterativo['tiempo_ns'].mean(), color='blue', linestyle='--')
plt.axhline(cifrado_asim_iterativo['tiempo_ns'].mean(), color='orange', linestyle='--')
plt.axhline(cifrado_sim_2_4['tiempo_ns'].mean(), color='blue', linestyle='--')
plt.axhline(cifrado_asim_2_4['tiempo_ns'].mean(), color='orange', linestyle='--')
plt.axhline(cifrado_sim_2_16['tiempo_ns'].mean(), color='blue', linestyle='--')
plt.axhline(cifrado_asim_2_16['tiempo_ns'].mean(), color='orange', linestyle='--')
plt.axhline(cifrado_sim_2_32['tiempo_ns'].mean(), color='blue', linestyle='--')
plt.axhline(cifrado_asim_2_32['tiempo_ns'].mean(), color='orange', linestyle='--')
plt.axhline(cifrado_sim_2_64['tiempo_ns'].mean(), color='blue', linestyle='--')
plt.axhline(cifrado_asim_2_64['tiempo_ns'].mean(), color='orange', linestyle='--')

plt.title('Comparación de tiempos de Cifrado Simétrico vs Asimétrico en todos los escenarios')
plt.xlabel('Mediciones')
plt.ylabel('Tiempo de cifrado (ns)')
plt.yscale('log')
plt.grid(True)
plt.legend()
plt.show()