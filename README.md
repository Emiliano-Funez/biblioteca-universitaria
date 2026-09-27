Proyecto final de Programación Orientada a Objetos - Java.

1. Modelo de clases

El proyecto está dividido en clases según la función que cumple cada una.

# Usuarios 

Usuario es una clase abstracta que contiene los datos y comportamientos 
comunes de los usuarios de la biblioteca, cada tipo de usuario tiene distintas reglas para los préstamos.

De ella heredan:

- Estudiante
- Docente

# Materiales

(MaterialBibliografico) es una clase abstracta que representa un material
de la biblioteca, ambas clases manejan el código, título y cantidad disponible del material.

De ella heredan:

- Libro
- Revista

# Préstamos y multas

- Prestamo representa un préstamo realizado a un usuario y guarda las fechas
de inicio, vencimiento y devolución.

- Multa representa una penalización generada cuando un préstamo es devuelto
fuera de término.

# Acceso a datos

Los DAO se encargan de comunicarse con la base de datos:

- UsuarioDAO
- MaterialDAO
- PrestamoDAO
- MultaDAO

- PrestamoService contiene las reglas necesarias para realizar préstamos y
devoluciones.

- MultaService controla las multas y la suspensión automática de usuarios.

- ConexionBD se encarga de realizar la conexión con MySQL.

2. Decisiones de diseño

Se utilizaron clases abstractas para representar conceptos generales y luego
crear clases más específicas mediante herencia. También se separó el acceso a la base de datos de la lógica del sistema
utilizando DAO y Services. De esta forma cada clase tiene una responsabilidad
más concreta.

Las reglas elegidas para el proyecto son:

- Estudiante: máximo 3 préstamos, 7 días y multa de $500 por día.
- Docente: máximo 5 préstamos, 14 días y multa de $300 por día.
- Si las multas pendientes superan los $5000, el usuario queda suspendido.

3. Herencia y polimorfismo

La herencia se aplica de la siguiente manera:

- public class Estudiante extends Usuario
- public class Docente extends Usuario
- public class Libro extends MaterialBibliografico
- public class Revista extends MaterialBibliografico

El polimorfismo se utiliza al trabajar con referencias de las clases
abstractas de la siguiente manera: 

Usuario usuario = new Estudiante()
MaterialBibliografico material = new Libro();

4. Instrucciones de ejecución

Base de datos
a. Abrir MySQL Workbench.
b. Ejecutar el archivo database/schema.sql.
c. Verificar que exista la base biblioteca_universitaria.
d. Configurar la variable de entorno con la contraseña de MySQL: setx BIBLIOTECA_DB_PASSWORD "442200Emi-!"

Después de configurarla, cerrar y volver a abrir VS Code.

Compilar

Desde la carpeta donde se encuentra C:\Users\emili\OneDrive\Desktop\Proyecto final\Proyecto\biblioteca-universitaria-final\biblioteca-universitaria:
mvn clean compile

- Ejecutar
mvn exec:java

y desde ahi ya se podria ver el proyecto funcionando. 