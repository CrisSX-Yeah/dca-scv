[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/n6fMhf_d)
# Aplicación inicial ToDoList

## Enlace al repositorio de DockerHub

Puedes encontrar la imagen Docker de este proyecto en DockerHub en el siguiente enlace:

[Repositorio de DockerHub - bledyx/mads-todolist](https://hub.docker.com/r/bledyx/mads-todolist/tags)

Aplicación ToDoList de la asignatura [MADS](https://cvnet.cpd.ua.es/Guia-Docente/GuiaDocente/Index?wlengua=es&wcodasi=34037&scaca=2024-25) usando Spring Boot y plantillas Thymeleaf.

## Requisitos

Necesitas tener instalado en tu sistema:

- Java 8

## Ejecución

Puedes ejecutar la aplicación usando el _goal_ `run` del _plugin_ Maven 
de Spring Boot:

```
$ ./mvnw spring-boot:run 
```   

También puedes generar un `jar` y ejecutarlo:

```
$ ./mvnw package
$ java -jar target/mads-todolist-inicial-0.0.1-SNAPSHOT.jar 
```

Una vez lanzada la aplicación puedes abrir un navegador y probar la página de inicio:

- [http://localhost:8080/login](http://localhost:8080/login)
