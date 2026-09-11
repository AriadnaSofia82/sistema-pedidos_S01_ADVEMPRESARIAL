# Sistema de Pedidos - ISIL

Aplicación web desarrollada para la implementación de un sistema de gestión de pedidos utilizando Jakarta EE, Servlets, EJB y JPA, aplicando una arquitectura distribuida por capas.

El proyecto permite registrar, consultar, editar y eliminar pedidos, manteniendo la consistencia del stock de los productos mediante transacciones administradas por el servidor.

## **1. Información del proyecto**

Proyecto: Sistema de Pedidos
Curso: Advanced Business Application Development
Institución: ISIL
Tecnologías principales: Java, Jakarta EE, Servlet, EJB, JPA, H2, HTML, CSS y JavaScript.

## **2. Objetivo**

El objetivo del proyecto es implementar las operaciones principales de gestión de pedidos:

- Registrar pedidos.
- Consultar pedidos registrados.
- Editar pedidos.
- Eliminar pedidos.
- Actualizar correctamente el stock al modificar un pedido.
- Restaurar el stock correspondiente al eliminar un pedido.
- Recalcular automáticamente el total del pedido.
- Validar los datos ingresados.
- Utilizar transacciones administradas por el servidor.
- Implementar solicitudes HTTP PUT y DELETE mediante JavaScript.
- Mantener la lógica de negocio dentro de la capa Business.

## **3. Arquitectura**

El proyecto mantiene la separación de responsabilidades mediante las siguientes capas:
CLIENT TIER: 
- Navegador web
- HTML + JavaScript + Fetch 
--> HTTP 
--> WEB TIER: 
- PedidoServlet
- GET / POST / PUT / DELETE
--> EJB
--> BUSINESS TIER:
- PedidoService
- Validaciones + reglas de negocio + transacciones
 --> JPA
--> EIS TIER:
- Base H2
- Persistencia JPA

Flujo principal
Navegador -> PedidoServlet -> PedidoService (EJB) -> EntityManager / JPA -> Base de datos H2

La lógica de negocio no se implementa directamente en el Servlet. El PedidoServlet recibe las solicitudes HTTP y delega las operaciones al PedidoService.

## **4. Estructura del proyecto**
   sistema-pedidos
      src
         main
            java
               pe.edu.isil.pedidos
                  domain
                     Pedido.java
                     Producto.java
                  service
                     PedidoService.java
                  web
                     PedidoServlet.java
            resources
               META-INF
                  persistence.xml
            webapp
               index.html
      pom.xml
      .gitignore
      README.md 

Descripción de las clases principales
Pedido.java:
Entidad JPA que representa un pedido realizado por un cliente.
Contiene información como:
- ID del pedido
- Cliente
- Producto
- Cantidad
- Total
- Fecha

Producto.java
Entidad JPA que representa los productos disponibles.
Contiene:
- ID
- Nombre
- Precio
- Stock

También contiene las operaciones relacionadas con el stock:
descontarStock(int cantidad) y aumentarStock(int cantidad)
Estas operaciones permiten mantener las reglas de stock centralizadas en la entidad.

PedidoService.java
Es la capa de negocio del sistema. Se encuentra implementada como un EJB Stateless:
@Stateless
public class PedidoService

Aquí se encuentran las operaciones:
- registrarPedido()
- actualizarPedido()
- eliminarPedido()
- listarProductos()
- listarPedidos()
También se encuentran las validaciones y el control de las transacciones.

PedidoServlet.java
Es el componente de la capa Web.
Se encuentra publicado mediante:
@WebServlet("/pedidos")

Procesa las siguientes solicitudes:

| Método HTTP	  |    Operación |
|----------------|------------------|
| GET	          |    Consultar pedidos y productos|
| POST	          |    Registrar pedido |
| PUT	          |    Editar pedido|
| DELETE	      |    Eliminar pedido|

## **5. Funcionalidades implementadas**
### **5.1 Registrar pedido**
   El usuario puede registrar un pedido indicando:
   - Cliente.
   - Producto.
   - Cantidad.

   Al registrar el pedido:
   1. Se valida la información.
   2. Se obtiene el producto.
   3. Se verifica el stock.
   4. Se descuenta el stock.
   5. Se calcula el total.
   6. Se persiste el pedido.

   El total se calcula mediante: total = precio del producto × cantidad

### **5.2 Editar pedido**
   La edición se realiza utilizando una solicitud HTTP PUT. 
   Desde la interfaz se ejecuta:
   
   fetch("pedidos?id=" + id + "...", {
      method: "PUT"
   })
   
   La solicitud es recibida por:
   doPut()
   del PedidoServlet.
   
   Posteriormente, el Servlet delega la operación:
   pedidoService.actualizarPedido(
      id,
      cliente,
      productoId,
      cantidad
   );
   
   La lógica de actualización permanece en la capa Business.

### **5.3 Control de stock al editar**
   El sistema considera dos situaciones.
   Caso 1: Se mantiene el mismo producto
   Se calcula la diferencia entre la nueva cantidad y la cantidad anterior:
   diferencia = nueva cantidad - cantidad anterior
   Si la nueva cantidad es mayor:
   stock = stock - diferencia
   Si la nueva cantidad es menor:
   stock = stock + diferencia absoluta
   Ejemplo:
   Stock inicial:       10
   Pedido anterior:      2
   Nueva cantidad:       5
   
   Diferencia: 5 - 2 = 3
   Nuevo stock: 10 - 3 = 7
   
   Si se reduce la cantidad:
   Stock actual:        7
   Cantidad anterior:   5
   Nueva cantidad:      3
   
   Se devuelven 2 unidades al stock.
   Nuevo stock: 9
   
   Caso 2: Se cambia de producto
   Cuando el usuario cambia el producto del pedido:
   Se devuelve al stock del producto anterior la cantidad que tenía el pedido.
   Se descuenta del nuevo producto la nueva cantidad.
   Se actualiza el pedido.
   Se recalcula el total utilizando el precio del nuevo producto.
   
   Ejemplo:
   Producto anterior: Laptop
   Cantidad anterior: 2
   
   Producto nuevo: Monitor
   Nueva cantidad: 3
   
   Resultado:
   Laptop  → +2 unidades
   Monitor → -3 unidades

### **5.4 Recalcular total**
   Al modificar un pedido, el total se calcula nuevamente utilizando el precio actual del producto y la nueva cantidad:
   
   BigDecimal total = productoNuevo
   .getPrecio()
   .multiply(BigDecimal.valueOf(cantidad));
   
   De esta manera, el total no depende del valor anterior del pedido.

### **5.5 Eliminar pedido**
   La eliminación utiliza una solicitud HTTP DELETE.
   Desde JavaScript:
   fetch("pedidos?id=" + id, {
   method: "DELETE"
   })
   
   La solicitud es procesada por:
   doDelete()
   del PedidoServlet.
   Luego se delega al servicio:
   pedidoService.eliminarPedido(id);
   
   Antes de eliminar el pedido, se devuelve al stock la cantidad correspondiente.
   Ejemplo:
   Stock actual:       5
   Cantidad del pedido: 2
   Nuevo stock:        7
   Finalmente, el pedido es eliminado mediante JPA.

## **6. Validaciones**
El sistema realiza validaciones en la capa Business.

Cliente
No puede estar vacío:

if (cliente == null || cliente.isBlank()) {
   throw new IllegalArgumentException(
   "El cliente es obligatorio."
   );
}
Producto
Se valida que exista un producto seleccionado.
Además, se verifica que el producto exista en la base de datos.

Cantidad
La cantidad debe ser mayor que cero:
if (cantidad <= 0) {
   throw new IllegalArgumentException(
   "La cantidad debe ser mayor que cero."
   );
}
Pedido
Antes de editar o eliminar se verifica que el pedido exista.
Stock
Antes de descontar unidades se verifica que exista stock suficiente.
Si no existe suficiente stock, se genera un error:
Stock insuficiente. Disponible: X

## **7. Transacciones**

El sistema utiliza transacciones administradas por el servidor mediante EJB.
Las operaciones de registro, actualización y eliminación utilizan:
@TransactionAttribute(TransactionAttributeType.REQUIRED)

Por ejemplo:
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public Pedido actualizarPedido(...) {
   ...
}

Esto permite que las operaciones relacionadas con el pedido y el stock formen parte de una misma transacción.
Por ejemplo, al cambiar un pedido de producto:

Actualizar stock producto anterior
   +
Actualizar stock producto nuevo
   +
Actualizar pedido
   +
Recalcular total

Las operaciones se ejecutan dentro de la misma transacción.
Esto permite mantener la consistencia de los datos ante errores durante la operación.

## **8. Persistencia**

La persistencia se implementa utilizando:
- JPA.
- EntityManager.
- H2 Database.
- persistence.xml.

El servicio utiliza:
@PersistenceContext(unitName = "PedidosPU")
private EntityManager entityManager;

Las entidades principales son:
Pedido
Producto

## **9. Métodos HTTP implementados**

El Servlet utiliza el siguiente endpoint:
/pedidos
GET

Consulta productos y pedidos:
GET /pedidos
POST

Registra un nuevo pedido:
POST /pedidos
PUT

Actualiza un pedido:
PUT /pedidos?id=1&cliente=Ana&productoId=2&cantidad=3
DELETE

Elimina un pedido:
DELETE /pedidos?id=1

## **10. JavaScript y Fetch API**

La edición y eliminación utilizan JavaScript mediante la API fetch().

PUT
fetch(
   "pedidos?id=" + encodeURIComponent(id)
      + "&cliente=" + encodeURIComponent(cliente)
      + "&productoId=" + encodeURIComponent(productoId)
      + "&cantidad=" + encodeURIComponent(cantidad),
   {
   method: "PUT"
   }
);
DELETE
fetch(
   "pedidos?id=" + encodeURIComponent(id),
   {
      method: "DELETE"
   }
);

De esta forma se utilizan solicitudes HTTP reales PUT y DELETE, procesadas respectivamente mediante:
doPut() y doDelete()

## **11. Requisitos para ejecutar el proyecto**
Se requiere tener instalado:
- JDK compatible con la versión configurada en pom.xml.
- Apache Maven.
- Un servidor compatible con Jakarta EE.
- Un navegador web.
- Git, para clonar y publicar el proyecto.

También se requiere configurar correctamente el servidor Jakarta EE utilizado para ejecutar la aplicación.

## **12. Instalación**
Clonar el repositorio:
git clone URL_DEL_REPOSITORIO

Ingresar a la carpeta:
cd sistema-pedidos

Limpiar y compilar el proyecto:
mvn clean package

Si la compilación finaliza correctamente, se generará el archivo correspondiente dentro de:
target/

Posteriormente, desplegar la aplicación en el servidor Jakarta EE configurado.

## **13. Ejecución**
Una vez desplegada la aplicación, ingresar mediante el navegador a la URL correspondiente al servidor.

La aplicación presenta una página inicial:
Sistema de Pedidos - ISIL

Desde allí se puede acceder a:
/pedidos

En la pantalla principal se muestran:
- Formulario de registro.
- Lista de productos.
- Stock disponible.
- Pedidos registrados.
- Total de cada pedido.
- Botón Editar.
- Botón Eliminar.

## **14. Pruebas funcionales**

Para verificar el correcto funcionamiento del sistema se recomienda realizar las siguientes pruebas.

Prueba 1: Registrar pedido
1. Seleccionar un producto.
2. Ingresar un cliente.
3. Ingresar una cantidad válida.
4. Presionar Registrar.
5. Verificar que el pedido aparezca en la tabla.
6. Verificar que el stock haya disminuido.

Resultado esperado:
El pedido se registra correctamente y el stock se actualiza.

Prueba 2: Editar cantidad del mismo producto
1. Registrar un pedido.
2. Presionar Editar.
3. Mantener el mismo producto.
4. Aumentar la cantidad.
5. Confirmar la operación.
6. Verificar el nuevo total.
7. Verificar el nuevo stock.

Resultado esperado:
El stock debe disminuir únicamente por la diferencia entre la nueva cantidad y la anterior.

Prueba 3: Reducir cantidad del mismo producto
1. Seleccionar un pedido existente.
2. Presionar Editar.
3. Mantener el mismo producto.
4. Reducir la cantidad.
5. Confirmar.
6. Verificar el stock.

Resultado esperado:
Las unidades reducidas deben regresar al stock.

Prueba 4: Cambiar de producto
1. Registrar un pedido con un producto.
2. Presionar Editar.
3. Seleccionar otro producto.
4. Indicar una nueva cantidad.
5. Confirmar.

Resultado esperado:
Producto anterior → recupera la cantidad anterior.
Producto nuevo    → descuenta la nueva cantidad.
Pedido            → cambia de producto.
Total             → se recalcula.

Prueba 5: Intentar superar el stock
1. Seleccionar un pedido.
2. Presionar Editar.
3. Indicar una cantidad superior al stock disponible.
4. Confirmar.

Resultado esperado:
La operación debe ser rechazada y mostrar un mensaje similar a:
Stock insuficiente. Disponible: X
El pedido y el stock deben permanecer consistentes.

Prueba 6: Eliminar pedido
1. Registrar un pedido.
2. Presionar Eliminar.
3. Confirmar la eliminación.
4. Verificar que el pedido desaparezca.

Resultado esperado:
El pedido se elimina y las unidades utilizadas por dicho pedido regresan al stock.

Prueba 7: Validar cantidad
Intentar registrar o editar un pedido con:
0
o un número negativo.
Resultado esperado:
La operación debe ser rechazada con:
La cantidad debe ser mayor que cero.

Prueba 8: Validar pedido inexistente
Enviar una solicitud utilizando un ID que no existe.
Ejemplo:
DELETE /pedidos?id=9999
Resultado esperado:
La operación debe ser rechazada indicando que el pedido no existe.

## **15. Verificación de PUT y DELETE**
Para demostrar que se implementaron los métodos HTTP solicitados, se puede utilizar las herramientas de desarrollador del navegador.

Pasos
1. Abrir la aplicación.
2. Presionar F12.
3. Ir a la pestaña Network / Red.
4. Editar un pedido.
5. Buscar la solicitud pedidos.
6. Verificar:
7. Request Method: PUT

Posteriormente:

1. Eliminar otro pedido.
2. Revisar nuevamente Network / Red.
3. Verificar:
Request Method: DELETE

Esto permite evidenciar que no se trata simplemente de botones HTML, sino de solicitudes HTTP PUT y DELETE reales.

## **16. Códigos HTTP utilizados**
El Servlet utiliza códigos de respuesta HTTP de acuerdo con el resultado de la operación.

| Código	          | Situación                             |
|------------------|---------------------------------------|
| 200 OK	          | Pedido actualizado correctamente      |
| 204 No Content   | Pedido eliminado correctamente        |
| 400 Bad Request	 | Datos inválidos o error de validación |

## **17. Manejo de errores**
Los errores de negocio son manejados mediante excepciones:
IllegalArgumentException y IllegalStateException

Por ejemplo:
El cliente es obligatorio.
El producto no existe.
La cantidad debe ser mayor que cero.
El pedido no existe.
Stock insuficiente.

El Servlet transforma estos errores en respuestas HTTP 400 Bad Request.

## **18. Criterios de implementación**
El proyecto cumple con los siguientes puntos:

| Requisito	                                 | 	Implementación |
|--------------------------------------------|-----------------|
| Registrar pedidos	                         | 	POST           |
| Consultar pedidos	                         | 	GET            |
| Editar pedidos	                            | 	PUT            |
| Eliminar pedidos	                          | 	DELETE         |
| doPut()	                                   | 	Implementado   |
| doDelete()	                                | 	Implementado   |
| JavaScript fetch()	                        | 	Implementado   |
| Validación de cantidad	                    | 	Implementada   |
| Validación de producto	                    | 	Implementada   |
| Validación de pedido	                      | 	Implementada   |
| Control de stock	                          | 	Implementado   |
| Recalcular total	                          | 	Implementado   |
| Restaurar stock al eliminar	               | 	Implementado   |
| JPA	                                       | Implementado    |
| EJB Stateless	           	           	     | 	Implementado   |
| Transacciones administradas por servidor 	 | 	Implementado   |
| Lógica de negocio en Service	              | 	Implementado   |
| Persistencia H2	                           | 	Implementada   |
| README	                                    | 	Implementado   |

## **Conclusión**

El proyecto implementa un sistema web para la gestión de pedidos utilizando Jakarta EE y una arquitectura separada por capas.
La solución permite registrar, consultar, editar y eliminar pedidos. La edición y eliminación se realizan mediante solicitudes HTTP reales PUT y DELETE, procesadas por doPut() y doDelete() del PedidoServlet.
La lógica de negocio se mantiene en PedidoService, donde se realizan las validaciones, actualización del stock y cálculo del total. Asimismo, las operaciones utilizan transacciones administradas por el servidor mediante EJB, permitiendo mantener la consistencia entre los pedidos y el stock de los productos.
De esta manera, la implementación cumple con los principales requerimientos funcionales y técnicos establecidos para el proyecto.