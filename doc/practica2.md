# Documentation of Practice 2 MADS Cristian Andrés Córdoba Silvestre (DNI 05987721G)

## URL of the Original Github Repository
[https://github.com/mads-UA-24-25/todo-list-CrisSX-Yeah](https://github.com/mads-UA-24-25/todo-list-CrisSX-Yeah)

## URL of DockerHub
[https://hub.docker.com/r/bledyx/mads-todolist](https://hub.docker.com/r/bledyx/mads-todolist)

## URL of Trello's Board Project
[https://trello.com/b/eBZ7BFYS/todolist-mads](https://trello.com/b/eBZ7BFYS/todolist-mads)

# IMPORTANT
As I have commented you in the class, I had problems with the share of the GitHub Board Project, so I had to create a GitHub Board Project in a parallel GitHub Repository.

All the issues and pull requests are included in [the original GitHub repository](https://github.com/mads-UA-24-25/todo-list-CrisSX-Yeah).

## The link to the complete GitHub Board Project is the following:

[https://github.com/users/CrisSX-Yeah/projects/5](https://github.com/users/CrisSX-Yeah/projects/5)

## The link to the GitHub repository associated with the completed Board Project is the following:

[https://github.com/CrisSX-Yeah/Mine-MADS-Prac2](https://github.com/CrisSX-Yeah/Mine-MADS-Prac2)






# ToDoList Application: Features Implemented in 1.1.0 release

## "002 NavBar" Feature

### Overview
This update introduces a dynamic **Menu Bar** to the ToDoList application, enhancing navigation and user experience. The menu bar is conditionally rendered based on the user's authentication status, ensuring secure and intuitive access to application features.

### Classes and Methods Created

#### `src/main/java/madstodolist/controller/GlobalControllerAdvice.java`
- **Purpose:** Automatically adds the `usuario` object to the model for all controllers if a user is logged in.
- **Key Method:**
  - `addAttributes(Model model)`: Checks the session for a logged-in user and adds the `usuario` attribute to the model.

#### `src/main/java/madstodolist/authentication/ManagerUserSession.java`
- **Purpose:** Manages user session attributes.
- **Key Methods:**
  - `logearUsuario(Long idUsuario)`: Stores the user's ID in the session upon login.
  - `usuarioLogeado()`: Retrieves the logged-in user's ID from the session.
  - `logout()`: Clears the user's ID from the session.

### Classes and Methods Modified

#### `src/main/java/madstodolist/controller/HomeController.java`
- **Purpose of the modification:** Adds user information to the `/about` page to facilitate menu bar rendering.
- **Modified or created Methods:**
  - `about(Model model)`: Retrieves the logged-in user's data and adds it to the model for the about page.


#### `src/main/java/madstodolist/controller/LoginController.java`
- **Purpose of the modification:** Adds user information to the model post-login to facilitate menu bar rendering.
- **Modified or created Methods:**
  - `loginSubmit(LoginData loginData, Model model, HttpSession session)`: Authenticates the user, stores session data, and adds `usuario` to the model upon successful login.
  - `logout(HttpSession session)`: Clears the user session and redirects to the login page.

### New Thymeleaf Templates

#### `src/main/resources/templates/fragments.html`
- **Purpose:** Contains reusable Thymeleaf fragments, including the new `menuBar` fragment.
- **Key Features:**
  - **`menuBar` Fragment:** Implements a Bootstrap Navbar with links that adapt based on the user's login status.
  - **Conditional Rendering:** Uses Thymeleaf expressions (`th:if` and `th:unless`) to display menu items dynamically.

### Updated Thymeleaf Templates
- **Purpose of the modification:** Integrates the `menuBar` fragment into various templates to provide a consistent navigation experience across the application.
- **Modified templates:**
  - **Included Menu Bar:**
    - `src/main/resources/templates/about.html`
    - `src/main/resources/templates/listaTareas.html`
    - `src/main/resources/templates/formEditarTarea.html`
    - `src/main/resources/templates/formNuevaTarea.html`
  - **Excluded Menu Bar:**
    - `src/main/resources/templates/formLogin.html`
    - `src/main/resources/templates/formRegistro.html`

### Tests Implemented

#### `src/test/java/madstodolist/controller/MenuBarControllerTest.java`
- **Purpose:** Verifies the correct rendering of the menu bar based on user authentication status.
- **Key Tests:**
  - **Not Logged In:** Ensures "Login" and "Register" links are visible.
  - **Logged In:** Ensures the username and "Log out" option are visible.
  - **Access Control:** Confirms that accessing the tasks page redirects to login when not authenticated.

#### `src/test/java/madstodolist/service/UsuarioServiceTest.java`
- **Purpose:** Tests user-related functionalities in the service layer.
- **Key Tests:**
  - **Login Scenarios:** Validates successful login, user not found, and incorrect password cases.
  - **User Retrieval:** Checks retrieval of user data by ID.

#### `src/test/java/madstodolist/repository/UsuarioRepositoryTest.java`
- **Purpose:** Ensures the repository correctly handles user data operations.
- **Key Tests:**
  - **Find by Email:** Confirms behavior when searching for existing and non-existing emails.

### Example Source Code

#### `src/main/resources/templates/fragments.html` - Menu Bar Fragment
As we have seen, the `fragments.html` file contains the `menuBar` fragment, which is included in various templates to provide a consistent navbar experience.

```html
<div th:fragment="menuBar">
    <nav class="navbar navbar-expand-lg navbar-light bg-light">
        <a class="navbar-brand" th:href="@{/about}">ToDoList</a>
        <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav"
                aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav mr-auto">
                <li class="nav-item">
                    <a class="nav-link" th:href="@{/about}">ToDoList</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" th:href="@{/usuarios/{id}/tareas(id=${usuario?.id})}">Tasks</a>
                </li>
            </ul>
            <ul class="navbar-nav">
                <li class="nav-item dropdown" th:if="${usuario != null}">
                    <a class="nav-link dropdown-toggle" href="#" id="navbarDropdown" role="button"
                       data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                        <span th:text="${usuario.nombre}">Username</span>
                    </a>
                    <div class="dropdown-menu dropdown-menu-right" aria-labelledby="navbarDropdown">
                        <a class="dropdown-item" href="#">Account</a>
                        <div class="dropdown-divider"></div>
                        <a class="dropdown-item" th:href="@{/logout}">Log out</a>
                    </div>
                </li>
                <li class="nav-item" th:unless="${usuario != null}">
                    <a class="nav-link" th:href="@{/login}">Login</a>
                </li>
                <li class="nav-item" th:unless="${usuario != null}">
                    <a class="nav-link" th:href="@{/registro}">Register</a>
                </li>
            </ul>
        </div>
    </nav>
</div>
```


## "003 List registered users" Feature

### Overview
This feature will display each **user's unique identifier (ID)** and **email address**, along with **pagination** to handle large user lists.

### Classes and Methods Created

#### `src/main/java/madstodolist/controller/UserController.java`
- **Purpose:** New controller added to manage requests to the `/registrados` endpoint.
- **Key Methods:**
  - `String listarUsuarios(Model model,@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size)`: Pageable Object: Using PageRequest.of(page, size) to define pagination settings.


### Classes and Methods Modified

#### `src/main/java/madstodolist/repository/UsuarioRepository.java`
- **Purpose of the modification:** To enable pagination, now `UsuarioRepository` extends `JpaRepository` instead of `CrudRepository` .

#### `src/main/java/madstodolist/service/UsuarioService.java`
- **Purpose of the modification:** Added a new method to fetch users in a paginated manner.
- **Modified or created Methods:**
  - `Page<UsuarioData> listarUsuarios(Pageable pageable)`: Retrieves a paginated list of Usuario entities and maps them to UsuarioData DTOs.



### New Thymeleaf Templates

#### `src/main/resources/templates/listaUsuarios.html`
- **Purpose:** This template displays the list of registered users along with pagination controls.
- **Key Features:**
  - **User List Table:** Iterates over `usuariosPage.content` to display each user's ID and email.
  - **Pagination Controls:** Generates a sequence of page numbers based on `usuariosPage.totalPages`.


### Updated Thymeleaf Templates
#### `src/main/resources/templates/fragments.html`
- **Purpose of the modification:** To provide easy access to the registered users list, we have added a "Registered Users" link in the menu bar.
- **Key Features:**
  - **"Registrados" Link:** Added a new `<li>` element with a link to `/registrados` labeled "Registrados". This provides easy navigation to the registered users list.



### Tests Implemented

#### `src/test/java/madstodolist/service/UsuarioServiceTest.java`
- **Purpose:** Ensure that the service logic of the new feature is correct.
  - **servicioListarUsuariosPaginado:** `listarUsuarios()` is called with a Pageable object specifying page 0 and size 2.


#### `src/test/java/madstodolist/controller/UserControllerTest.java`
- **Purpose:** Ensure that the new ´/registrados´ page functions correctly.
- **Key Tests:**
  - **listarUsuarios_ShouldReturnUsersList:** Verifies that the `/registrados` endpoint returns the correct view and model attributes with the list of users.
  - **listarUsuarios_WithPagination_ShouldReturnCorrectPage:** Ensures that pagination parameters (`page` and `size`) correctly affect the returned user list.

### Example Source Code

#### `src/main/java/madstodolist/controller/UserController.java`
New controller added to manage requests to the `/registrados` endpoint.

```java
package madstodolist.controller;

import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
// Import necessary Spring MVC annotations and classes
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/registrados")
    public String listarUsuarios(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UsuarioData> usuariosPage = usuarioService.listarUsuarios(pageable);
        model.addAttribute("usuariosPage", usuariosPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usuariosPage.getTotalPages());
        model.addAttribute("totalElements", usuariosPage.getTotalElements());
        return "listaUsuarios";
    }

    @GetMapping("/registrados/{id}")
    public String descripcionUsuario(@PathVariable Long id, Model model) {
        UsuarioData usuario = usuarioService.findById(id);
        if (usuario == null) {
            // Option 1: Redirect to a 404 error page
            return "error/404"; // Ensure you have a 404.html in src/main/resources/templates/error/

            // Option 2: Redirect to the user list with an error message
            // model.addAttribute("errorMessage", "Usuario no encontrado.");
            // return "redirect:/registrados";
        }
        model.addAttribute("usuario", usuario);
        return "usuarioDescripcion";
    }

    @PostMapping("/registrados/{id}/toggleBlock")
    public String toggleUserBlockedStatus(@PathVariable Long id) {
        usuarioService.toggleUserBlockedStatus(id);
        return "redirect:/registrados";
    }

}
```


## "004 User's description" Feature

### Overview
This feature includes adding a link in the user list to access detailed information about each user, excluding their password.

### Classes and Methods Created

#### `src/main/java/madstodolist/controller/UserController.java`
- **Purpose:** New controller added to manage requests to the `/registrados` endpoint.
- **Key Methods:**
  - `String listarUsuarios(Model model,@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size)`: Pageable Object: Using PageRequest.of(page, size) to define pagination settings.


### Classes and Methods Modified

#### `src/main/java/madstodolist/controller/UserController.java`
- **Purpose of the modification:** Add a new method to handle GET requests to `/registrados/{id}` and return the user's description.
- **Modified or created Methods:**
  - `String descripcionUsuario(@PathVariable Long id, Model model)`: Uses `usuarioService.findById(id)` to fetch user details.


### New Thymeleaf Templates

#### `src/main/resources/templates/error/404.html`
- **Purpose:** To redirect to a custom 404 error page when a user is not found.
- **Key Features:**
  - **Navigation Button:** Allows users to return to the main user list.


### Updated Thymeleaf Templates
#### `src/main/resources/templates/listaUsuarios.html`
- **Purpose of the modification:** Added a new column in the user list table that contains links to access each user's description.
- **Key Features:**
  - **New Column Header:** Added a "Descripción" column to hold the links.
  - **Link Implementation:** For each user, a link labeled "Ver Descripción" directs to `/registrados/{id}`, where `{id}` is the user's unique identifier.


### Tests Implemented

#### `src/test/java/madstodolist/controller/UserControllerTest.java`
- **Purpose:** To ensure that the new endpoint `/registrados/{id}` works as expected.
- **Key Tests:**
  - **descripcionUsuario_Existente_ShouldReturnUserDescription:** Ensures that accessing `/registrados/{id}` with a valid user ID returns the correct user details.
  - **descripcionUsuario_NoExistente_ShouldReturn404:** Checks that accessing `/registrados/{id}` with a non-existent user ID handles the scenario appropriately, such as returning a 404 error page.
  - **descripcionUsuario_ConFechaNacimiento_ShouldDisplayFechaNacimiento:** Confirms that the user's date of birth is correctly displayed when available.


### Example Source Code

#### `src/main/java/madstodolist/controller/UserController.java`
New controller added to manage requests to the `/registrados` endpoint.

```java
package madstodolist.controller;

import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
// Import necessary Spring MVC annotations and classes
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/registrados")
    public String listarUsuarios(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UsuarioData> usuariosPage = usuarioService.listarUsuarios(pageable);
        model.addAttribute("usuariosPage", usuariosPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usuariosPage.getTotalPages());
        model.addAttribute("totalElements", usuariosPage.getTotalElements());
        return "listaUsuarios";
    }

    @GetMapping("/registrados/{id}")
    public String descripcionUsuario(@PathVariable Long id, Model model) {
        UsuarioData usuario = usuarioService.findById(id);
        if (usuario == null) {
            // Option 1: Redirect to a 404 error page
            return "error/404"; // Ensure you have a 404.html in src/main/resources/templates/error/

            // Option 2: Redirect to the user list with an error message
            // model.addAttribute("errorMessage", "Usuario no encontrado.");
            // return "redirect:/registrados";
        }
        model.addAttribute("usuario", usuario);
        return "usuarioDescripcion";
    }

    @PostMapping("/registrados/{id}/toggleBlock")
    public String toggleUserBlockedStatus(@PathVariable Long id) {
        usuarioService.toggleUserBlockedStatus(id);
        return "redirect:/registrados";
    }

}
```


## "005 Administrator Registration" Feature

### Overview
This feature includes a checkbox that will allow users to sign up as an administrator. Only one administrator can exist. If an administrator already exists, the checkbox will not appear on the registration page.


### Classes and Methods Modified

#### `src/main/java/madstodolist/model/Usuario.java`
- **Purpose of the modification:** Modified the `Usuario` Model to include an admin field.
- **Modified or created Methods:**
  - `Boolean getAdmin()`: getter method for the admin id field.
  - `void setAdmin(Boolean admin)`: setter method for the admin id field.

#### `src/main/java/madstodolist/dto/UsuarioData.java`
- **Purpose of the modification:** Update the `UsuarioData` DTO to include the admin field.
- **Modified or created Methods:**
  - `Boolean getAdmin()`: getter method for the admin id field.
  - `void setAdmin(Boolean admin)`: setter method for the admin id field.

#### `src/main/java/madstodolist/dto/RegistroData.java`
- **Purpose of the modification:** Update or Create the `RegistroData DTO` to include the admin field.
- **Modified or created Methods:**
  - `Boolean getAdmin()`: getter method for the admin id field.
  - `void setAdmin(Boolean admin)`: setter method for the admin id field.

#### `src/main/java/madstodolist/controller/LoginController.java`
- **Purpose of the modification:** Modified the `LoginController` to handle the admin registration logic.
- **Modified or created Methods:**
  - `String registroForm(Model model)`: Checks if an admin already exists using `usuarioService.existeAdministrador()`.
  - `String registro(@Valid @ModelAttribute RegistroData registroData, BindingResult result, Model model)`: Checks again if an admin already exists if the user is attempting to register as admin. Redirects the admin user to /registrados (user list) after successful registration.

#### `src/main/java/madstodolist/repository/UsuarioRepository.java`
- **Purpose of the modification:** Updated the `UsuarioRepository` to handle the admin logic.
- **Modified or created Methods:**
  - `boolean existsByAdminTrue()`: `existsByAdminTrue()` will generate a query to check if any user has admin = true.


#### `src/main/java/madstodolist/service/UsuarioService.java`
- **Purpose of the modification:** Updated the `UsuarioService` to handle the admin field during registration and ensure only one administrator exists.
- **Modified or created Methods:**
  - `UsuarioData registrar(RegistroData registroData)`: Now checks if admin registration is allowed.
  - `boolean existeAdministrador()`: Checks if an administrator already exists in the system. Calls `usuarioRepository.existsByAdminTrue()`.


### Updated Thymeleaf Templates
#### `src/main/resources/templates/formRegistro.html`
- **Purpose of the modification:** Added a checkbox for admin registration, which only appears if no administrator exists.
- **Key Features:**
  - **Admin Checkbox:** The checkbox for admin registration is wrapped in a conditional `th:if="${!adminExists}`".
  - **Controller Needs to Provide `adminExists`:** The controller handling the registration page must set the adminExists attribute based on whether an admin user exists in the system.


### Tests Implemented

#### `src/test/java/madstodolist/repository/UsuarioRepositoryTest.java`
- **Purpose:** To test that the repository logic is correctly managed.
- **Key Tests:**
  - **existsByAdminTrue():** Verifies that the method returns `false` when no admin exists. Verifies that the method returns `true` when an admin user exists.

#### `src/test/java/madstodolist/service/UsuarioServiceTest.java`
- **Purpose:** To test that the service logic is correctly managed.
- **Key Tests:**
  - **registrarAdmin_WhenNoAdminExists_ShouldSucceed():** Tests that registering an admin when none exists succeeds.
  - **registrarAdmin_WhenAdminExists_ShouldFail():** Tests that attempting to register a second admin throws an exception.
  - **registrarUser_WhenAdminExists_ShouldSucceed():** Tests that regular users can still register when an admin exists.

#### `src/test/java/madstodolist/controller/LoginControllerTest.java`
- **Purpose:** To test that the controller logic is correctly managed.
- **Key Tests:**
  - **registroForm_NoAdminExists_ShouldShowAdminCheckbox():** Verifies that the admin checkbox is displayed when no admin exists.
  - **registroForm_AdminExists_ShouldHideAdminCheckbox():** Verifies that the admin checkbox is hidden when an admin exists.
  - **registro_AdminRegistration_ShouldRedirectToRegistrados():** Tests successful admin registration and redirection to /registrados.
  - **registro_SecondAdminRegistration_ShouldShowError():** Tests that attempting to register a second admin shows an error message.

### Example Source Code

#### `src/main/java/madstodolist/controller/LoginController.java`
Key modification in the `LoginController` to handle the admin registration logic.

```java
//New Implemented Methods
@GetMapping("/registro")
public String registroForm(Model model) {
    RegistroData registroData = new RegistroData();
    model.addAttribute("registroData", registroData);

    // Check if an admin already exists
    boolean adminExists = usuarioService.existeAdministrador();
    model.addAttribute("adminExists", adminExists);

    return "formRegistro";
}

@PostMapping("/registro")
public String registro(@Valid @ModelAttribute RegistroData registroData,
                       BindingResult result, Model model) {
    if (result.hasErrors()) {
        return "formRegistro";
    }

    try {
        // Check if admin registration is allowed
        if (registroData.getAdmin() && usuarioService.existeAdministrador()) {
            model.addAttribute("error", "Ya existe un administrador en el sistema");
            return "formRegistro";
        }

        UsuarioData nuevoUsuario = usuarioService.registrar(registroData);

        // Redirect based on admin status
        if (nuevoUsuario.getAdmin()) {
            return "redirect:/registrados";
        } else {
            return "redirect:/login";
        }
    } catch (UsuarioServiceException e) {
        model.addAttribute("error", e.getMessage());
        return "formRegistro";
    }
}
```


## "006 Protection of the user listing and user description" Feature

### Overview
If a non-administrator user attempts to access the URLs `/registrados` or `/registrados/{id}`, the application will return an HTTP 404 not found error with a message indicating insufficient permissions.
In the implementation, I decided to include a HTTP 404 error instead of a HTTP 401 error. The reason for that is due to good practices. A HTTP 404 error gives less information about the application, which is better for security reasons.


### Classes and Methods Created

#### `src/main/java/madstodolist/controller/exception/UnauthorizedAccessException.java`
- **Purpose:** This exception will be thrown when a non-admin user attempts to access admin-protected pages.

#### `src/main/java/madstodolist/interceptor/AdminInterceptor.java`
- **Purpose:** This interceptor checks if the current user is logged in and is an administrator before allowing access to protected URLs.

#### `src/main/java/madstodolist/config/WebConfig.java`
- **Purpose:** Registers the AdminInterceptor to apply to all URLs under /registrados/.



### Classes and Methods Modified

#### `src/main/java/madstodolist/controller/GlobalExceptionHandler.java`
- **Purpose of the modification:** Added a new exception handler for UnauthorizedAccessException.
- **Modified or created Methods:**
  - `public ResponseEntity<String> handleUnauthorizedAccessException(UnauthorizedAccessException ex)`: Returns an HTTP 401 Unauthorized status with the exception message when UnauthorizedAccessException is thrown.


### Tests Implemented

#### `src/test/java/madstodolist/repository/UsuarioRepositoryTest.java`
- **Purpose:** Verify that findByAdminFalse returns only non-admin users.
- **Key Tests:**
  - **findByAdminFalse_ShouldReturnOnlyNonAdminUsers():** Create and save one admin user and two non-admin users. Retrieve non-admin users using findByAdminFalse. Assert that only non-admin users are returned.

#### `src/test/java/madstodolist/service/UsuarioServiceTest.java`
- **Purpose:** Ensure that listarUsuarios in the service returns only non-admin users.
- **Key Tests:**
- **listarUsuarios_ShouldReturnOnlyNonAdminUsers():** Register an admin user and two non-admin users. Call `listarUsuarios`. Assert that only non-admin users are included in the returned page.

#### `src/test/java/madstodolist/controller/UserControllerTest.java`
- **Purpose:** Verify that only admin users can access the `/registrados` and `/registrados/{id}` URLs.
- **Key Tests:**
  - **accederRegistrados_AdminUser_ShouldAllowAccess:** Verify that an admin user can access the `/registrados` page.
  - **accederRegistrados_NonAdminUser_ShouldReturnUnauthorized:** Ensure that a non-admin user receives a 401 Unauthorized error when accessing `/registrados`.
  - **accederUsuarioDescripcion_NonAdminUser_ShouldReturnUnauthorized:** Ensure that a non-admin user receives a 401 Unauthorized error when accessing `/registrados/{id}`.

### Example Source Code

#### `src/main/java/madstodolist/interceptor/AdminInterceptor.java`
Implemented an Admin Interceptor to Enforce Authorization

```java
package madstodolist.interceptor;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.NotFoundException;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interceptor to ensure that only admin users can access certain URLs.
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

  @Autowired
  private ManagerUserSession managerUserSession;

  @Autowired
  private UsuarioService usuarioService;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

    Long userId = managerUserSession.usuarioLogeado();
    if (userId == null) {
      // User not logged in
      throw new NotFoundException("Página no encontrada.");
    }

    UsuarioData usuario = usuarioService.findById(userId);
    if (usuario == null || !usuario.getAdmin()) {
      // User is not admin
      throw new NotFoundException("Página no encontrada.");
    }

    // User is admin, allow access
    return true;
  }

}
```

#### `src/main/java/madstodolist/config/WebConfig.java`
Code to Register the Admin Interceptor

```java
package madstodolist.config;

import madstodolist.interceptor.AdminInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
* Web configuration to register interceptors.
  */
  @Configuration
  public class WebConfig implements WebMvcConfigurer {

  @Autowired
  private AdminInterceptor adminInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
  // Apply AdminInterceptor to /registrados and /registrados/**
  registry.addInterceptor(adminInterceptor)
  .addPathPatterns("/registrados/**");
  }
  }
```


## "007 User blocking by administrator" Feature

### Overview
The administrator can block or unblock any normal user.

### Classes and Methods Created

#### `src/main/java/madstodolist/config/SecurityConfig.java`
- **Purpose:** Create a security configuration class to set up Spring Security and allow anonymous access to the endpoints.

#### `src/main/java/madstodolist/config/CsrfControllerAdvice.java`
- **Purpose:** Create a ControllerAdvice class that adds the CSRF token to the model, so Thymeleaf can access it.

#### `src/test/java/madstodolist/controller/TestCsrfControllerAdvice.java`
- **Purpose:**  Create a ControllerAdvice class specifically for your tests to inject a mock CSRF token into the model


### Classes and Methods Modified

#### `src/main/java/madstodolist/model/Usuario.java`
- **Purpose of the modification:** Added the blocked field and its getter and setter in Usuario.java.
- **Modified or created Methods:**
  - `Boolean getBlocked()`: Getter method for the blocked field.
  - `void setBlocked(Boolean blocked)`: Setter method for the blocked field.

#### `src/main/java/madstodolist/service/UsuarioService.java`
- **Purpose of the modification:** Check if the user is blocked and return USER_BLOCKED if so.
- **Modified or created Methods:**
  - ` void toggleUserBlockedStatus(Long userId)`: Method to block/unblock a user.

#### `src/main/java/madstodolist/controller/UserController.java`
- **Purpose of the modification:** Added a new method to handle the block/unblock action.
- **Modified or created Methods:**
  - `String toggleUserBlockedStatus(@PathVariable Long id)`: New method added to handle the block/unblock action.

#### `src/main/java/madstodolist/controller/LoginController.java`
- **Purpose of the modification:** Updated the `loginSubmit` method to handle the `USER_BLOCKED` status.
- **Modified or created Methods:**
  - `String loginSubmit(@ModelAttribute LoginData loginData, Model model, HttpSession session)`: Modified the method to display an appropriate error message when the user is blocked.

#### `src/main/java/madstodolist/dto/UsuarioData.java`
- **Purpose of the modification:** Updated the `UsuarioData.java` DTO file with the new `blocked` field added.
- **Modified or created Methods:**
  - `String loginSubmit(@ModelAttribute LoginData loginData, Model model, HttpSession session)`: Modified the method to display an appropriate error message when the user is blocked.


### Updated Thymeleaf Templates

#### `src/main/resources/templates/listaUsuarios.html`
- **Purpose of the modification:** Added a block/unblock button for each user.
- **Key Features:**
  - **Block/unblock button:** Added a "Descripción" column to hold the links. The button text changes based on the user's blocked status.
  - **CSRF token:** Since the app is using a form to submit a POST request, we need to include the CSRF token.

### Tests Implemented

#### `src/test/java/madstodolist/repository/UsuarioTest.java`
- **Purpose:** Test the repository layer for the block/unblock functionality.
- **Key Tests:**
  - **saveAndRetrieveBlockedStatus():** Save a user with blocked status and retrieve it. Assert that the blocked status is correctly saved and retrieved.

#### `src/test/java/madstodolist/service/UsuarioServiceTest.java`
- **Purpose:** Test the service layer for the block/unblock functionality.
- **Key Tests:**
  - **toggleUserBlockedStatus_ShouldToggleBlockedStatus:** Test the corresponding blocked status of a created user.
  - **login_WhenUserIsBlocked_ShouldReturnUserBlocked:** Test the corresponding blocked status of a created user.

#### `src/test/java/madstodolist/controller/UserControllerTest.java`
- **Purpose:** Test the controller layer for the block/unblock functionality.
- **Key Tests:**
  - **adminCanToggleUserBlockedStatus:** Test that an admin can block/unblock a user.
  - **nonAdminCannotToggleUserBlockedStatus:** Test that a non-admin user cannot block/unblock a user.

#### `src/test/java/madstodolist/controller/TestCsrfControllerAdvice.java`
- **Purpose:** Test the correct set-up of CSRF tokens.

### Example Source Code

#### `src/main/java/madstodolist/config/SecurityConfig.java`
Created a security configuration class to set up Spring Security and allow anonymous access to the endpoints.

```java
package madstodolist.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// Import the new WebSecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                // Permit all requests for now
                .antMatchers("/**").permitAll()
                .and()
                .csrf()
                .csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse());
    }
}
```

#### `src/main/java/madstodolist/config/CsrfControllerAdvice.java`
Created a `ControllerAdvice` class that adds the CSRF token to the model, so Thymeleaf can access it.

```java
package madstodolist.config;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;

@Component
@ControllerAdvice
public class CsrfControllerAdvice {

  @ModelAttribute
  public void addCsrfToken(Model model, HttpServletRequest request) {
    CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    model.addAttribute("_csrf", token);
  }
}
```

#### `src/test/java/madstodolist/controller/TestCsrfControllerAdvice.java`
Test the correct set-up of CSRF tokens.

```java
package madstodolist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// ControllerAdvice to add a mock CSRF token to the model during tests
@ControllerAdvice
public class TestCsrfControllerAdvice {

  @ModelAttribute
  public void addCsrfToken(org.springframework.ui.Model model) {
    model.addAttribute("_csrf", new MockCsrfToken());
  }

  // Inner class to mock CsrfToken
  public static class MockCsrfToken {
    public String getToken() {
      return "mockCsrfToken";
    }

    public String getHeaderName() {
      return "X-CSRF-TOKEN";
    }

    public String getParameterName() {
      return "_csrf";
    }
  }
}
```



