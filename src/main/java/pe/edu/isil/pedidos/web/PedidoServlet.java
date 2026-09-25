package pe.edu.isil.pedidos.web;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

import pe.edu.isil.pedidos.domain.Pedido;
import pe.edu.isil.pedidos.domain.Producto;
import pe.edu.isil.pedidos.service.PedidoService;

@WebServlet("/pedidos")
public class PedidoServlet extends HttpServlet {

  @EJB
  private PedidoService pedidoService;

  @Override
  protected void doGet(
          HttpServletRequest request,
          HttpServletResponse response)
          throws ServletException, IOException {

    String creado = request.getParameter("creado");
    String actualizado = request.getParameter("actualizado");
    String eliminado = request.getParameter("eliminado");

    String mensaje = null;

    /*
     * Mensaje después de registrar un pedido
     */
    if (creado != null && !creado.isBlank()) {

      mensaje =
              "Pedido registrado correctamente. ID: "
                      + creado;
    }

    /*
     * Mensaje después de actualizar un pedido
     */
    if (actualizado != null && !actualizado.isBlank()) {

      mensaje =
              "Pedido actualizado correctamente. ID: "
                      + actualizado;
    }

    /*
     * Mensaje después de eliminar un pedido
     */
    if (eliminado != null && !eliminado.isBlank()) {

      mensaje =
              "Pedido eliminado correctamente. ID: "
                      + eliminado;
    }

    renderizarPagina(
            response,
            null,
            mensaje
    );
  }

  @Override
  protected void doPost(
          HttpServletRequest request,
          HttpServletResponse response)
          throws ServletException, IOException {

    request.setCharacterEncoding(
            StandardCharsets.UTF_8.name()
    );

    try {

      String cliente =
              request.getParameter("cliente");

      String productoParametro =
              request.getParameter("productoId");

      String cantidadParametro =
              request.getParameter("cantidad");

      if (productoParametro == null ||
              productoParametro.isBlank()) {

        throw new IllegalArgumentException(
                "Debe seleccionar un producto."
        );
      }

      if (cantidadParametro == null ||
              cantidadParametro.isBlank()) {

        throw new IllegalArgumentException(
                "La cantidad es obligatoria."
        );
      }

      Long productoId =
              Long.valueOf(
                      productoParametro
              );

      int cantidad =
              Integer.parseInt(
                      cantidadParametro
              );

      Pedido pedido =
              pedidoService.registrarPedido(
                      cliente,
                      productoId,
                      cantidad
              );

      /*
       * Después de registrar:
       * /pedidos?creado=ID
       */
      response.sendRedirect(
              request.getContextPath()
                      + "/pedidos?creado="
                      + pedido.getId()
      );

    } catch (NumberFormatException e) {

      response.setStatus(
              HttpServletResponse.SC_BAD_REQUEST
      );

      renderizarPagina(
              response,
              "Producto o cantidad inválidos.",
              null
      );

    } catch (IllegalArgumentException e) {

      response.setStatus(
              HttpServletResponse.SC_BAD_REQUEST
      );

      renderizarPagina(
              response,
              e.getMessage(),
              null
      );

    } catch (IllegalStateException e) {

      response.setStatus(
              HttpServletResponse.SC_BAD_REQUEST
      );

      renderizarPagina(
              response,
              e.getMessage(),
              null
      );
    }
  }

  @Override
  protected void doPut(
          HttpServletRequest request,
          HttpServletResponse response)
          throws IOException {

    request.setCharacterEncoding(
            StandardCharsets.UTF_8.name()
    );

    try {

      String idParametro =
              request.getParameter("id");

      if (idParametro == null ||
              idParametro.isBlank()) {

        throw new IllegalArgumentException(
                "El ID del pedido es obligatorio."
        );
      }

      Long id =
              Long.valueOf(
                      idParametro
              );

      String cliente =
              request.getParameter("cliente");

      String productoParametro =
              request.getParameter("productoId");

      String cantidadParametro =
              request.getParameter("cantidad");

      Long productoId = null;

      if (productoParametro != null &&
              !productoParametro.isBlank()) {

        productoId =
                Long.valueOf(
                        productoParametro
                );
      }

      Integer cantidad = null;

      if (cantidadParametro != null &&
              !cantidadParametro.isBlank()) {

        cantidad =
                Integer.valueOf(
                        cantidadParametro
                );
      }

      Pedido pedido =
              pedidoService.actualizarPedido(
                      id,
                      cliente,
                      productoId,
                      cantidad
              );

      /*
       * Respuesta HTTP del PUT.
       *
       * IMPORTANTE:
       * El PUT NO hace redirect porque el JavaScript
       * necesita recibir la respuesta directamente.
       */
      response.reset();

      response.setStatus(
              HttpServletResponse.SC_OK
      );

      response.setContentType(
              "text/plain;charset=UTF-8"
      );

      try (PrintWriter out =
                   response.getWriter()) {

        out.println(
                "Pedido actualizado correctamente."
        );

        out.println(
                "ID: "
                        + pedido.getId()
        );

        out.println(
                "Cliente: "
                        + pedido.getCliente()
        );

        out.println(
                "Producto: "
                        + pedido.getProducto()
                        .getNombre()
        );

        out.println(
                "Cantidad: "
                        + pedido.getCantidad()
        );

        out.println(
                "Total: S/ "
                        + pedido.getTotal()
                        .toPlainString()
        );
      }

    } catch (NumberFormatException e) {

      enviarError(
              response,
              HttpServletResponse.SC_BAD_REQUEST,
              "Error de datos: ID, producto o cantidad inválidos."
      );

    } catch (IllegalArgumentException e) {

      enviarError(
              response,
              HttpServletResponse.SC_BAD_REQUEST,
              "Error de validación: "
                      + e.getMessage()
      );

    } catch (IllegalStateException e) {

      enviarError(
              response,
              HttpServletResponse.SC_BAD_REQUEST,
              "Error de stock: "
                      + e.getMessage()
      );

    } catch (Exception e) {

      enviarError(
              response,
              HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
              "Error interno al actualizar el pedido."
      );
    }
  }

  @Override
  protected void doDelete(
          HttpServletRequest request,
          HttpServletResponse response)
          throws IOException {

    request.setCharacterEncoding(
            StandardCharsets.UTF_8.name()
    );

    try {

      String idParametro =
              request.getParameter("id");

      if (idParametro == null ||
              idParametro.isBlank()) {

        throw new IllegalArgumentException(
                "El ID del pedido es obligatorio."
        );
      }

      Long id =
              Long.valueOf(
                      idParametro
              );

      pedidoService.eliminarPedido(id);

      response.setStatus(
              HttpServletResponse.SC_NO_CONTENT
      );

    } catch (NumberFormatException e) {

      enviarError(
              response,
              HttpServletResponse.SC_BAD_REQUEST,
              "El ID del pedido es inválido."
      );

    } catch (IllegalArgumentException e) {

      enviarError(
              response,
              HttpServletResponse.SC_BAD_REQUEST,
              e.getMessage()
      );

    } catch (IllegalStateException e) {

      enviarError(
              response,
              HttpServletResponse.SC_BAD_REQUEST,
              e.getMessage()
      );

    } catch (Exception e) {

      enviarError(
              response,
              HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
              "Error interno al eliminar el pedido."
      );
    }
  }

  private void enviarError(
          HttpServletResponse response,
          int status,
          String mensaje)
          throws IOException {

    response.reset();

    response.setStatus(status);

    response.setContentType(
            "text/plain;charset=UTF-8"
    );

    try (PrintWriter out =
                 response.getWriter()) {

      out.println(mensaje);
    }
  }

  private void renderizarPagina(
          HttpServletResponse response,
          String error,
          String mensaje)
          throws IOException {

    List<Producto> productos =
            pedidoService.listarProductos();

    List<Pedido> pedidos =
            pedidoService.listarPedidos();

    response.setContentType(
            "text/html;charset=UTF-8"
    );

    try (PrintWriter out =
                 response.getWriter()) {

      out.println("""
                    <!doctype html>
                    <html lang="es">

                    <head>

                        <meta charset="UTF-8">

                        <meta name="viewport"
                              content="width=device-width, initial-scale=1">

                        <title>Sistema de Pedidos - ISIL</title>

                        <style>

                            * {
                                box-sizing: border-box;
                            }

                            body {
                                font-family: Arial, sans-serif;
                                max-width: 1150px;
                                margin: 32px auto;
                                padding: 0 16px;
                                background: #f8f9fa;
                                color: #222;
                            }

                            h1 {
                                margin-bottom: 8px;
                            }

                            h2 {
                                margin-top: 30px;
                            }

                            form {
                                display: grid;
                                grid-template-columns:
                                    2fr 2fr 1fr auto;
                                gap: 12px;
                                align-items: end;
                            }

                            label {
                                display: flex;
                                flex-direction: column;
                                gap: 6px;
                                font-weight: 600;
                            }

                            input,
                            select,
                            button {
                                padding: 10px;
                                font-size: 14px;
                                border-radius: 5px;
                            }

                            input,
                            select {
                                border: 1px solid #bbb;
                                background: white;
                            }

                            button {
                                cursor: pointer;
                                border: none;
                            }

                            .btn-registrar {
                                background: #198754;
                                color: white;
                            }

                            .btn-editar {
                                background: #f0ad4e;
                                color: white;
                                margin-right: 5px;
                            }

                            .btn-eliminar {
                                background: #d9534f;
                                color: white;
                            }

                            .btn-guardar {
                                background: #0d6efd;
                                color: white;
                            }

                            .btn-cancelar {
                                background: #6c757d;
                                color: white;
                            }

                            table {
                                width: 100%;
                                border-collapse: collapse;
                                margin-top: 24px;
                                background: white;
                            }

                            th,
                            td {
                                border: 1px solid #ccc;
                                padding: 10px;
                                text-align: left;
                            }

                            th {
                                background: #e9ecef;
                            }

                            .error {
                                background: #ffe7e7;
                                border: 1px solid #d33;
                                padding: 12px;
                                margin: 16px 0;
                                border-radius: 5px;
                            }

                            .success {
                                background: #e7ffe7;
                                border: 1px solid #3a3;
                                padding: 12px;
                                margin: 16px 0;
                                border-radius: 5px;
                            }

                            .nota {
                                background: #e9ecef;
                                padding: 12px;
                                margin: 16px 0;
                                border-radius: 5px;
                            }

                            .edit-panel {
                                background: white;
                                border: 2px solid #0d6efd;
                                padding: 20px;
                                margin-top: 25px;
                                border-radius: 8px;
                            }

                            .edit-actions {
                                display: flex;
                                gap: 10px;
                                margin-top: 10px;
                            }

                            .acciones {
                                white-space: nowrap;
                            }

                            @media (max-width: 800px) {

                                form {
                                    grid-template-columns: 1fr;
                                }

                                table {
                                    display: block;
                                    overflow-x: auto;
                                }

                                .acciones {
                                    white-space: normal;
                                }
                            }

                        </style>

                    </head>

                    <body>

                        <h1>Sistema de Pedidos</h1>

                        <p class="nota">
                            Flujo:
                            Navegador →
                            PedidoServlet →
                            PedidoService (EJB) →
                            JPA →
                            H2
                        </p>
                    """);

      if (error != null) {

        out.printf(
                "<div class=\"error\">%s</div>%n",
                escapeHtml(error)
        );
      }

      if (mensaje != null) {

        out.printf(
                "<div class=\"success\">%s</div>%n",
                escapeHtml(mensaje)
        );
      }

      out.println("""
                        <h2>Registrar pedido</h2>

                        <form method="post">

                            <label>
                                Cliente

                                <input
                                    name="cliente"
                                    required
                                    maxlength="120"
                                    placeholder="Ej. Ana Torres">
                            </label>

                            <label>
                                Producto

                                <select
                                    name="productoId"
                                    required>
                    """);

      for (Producto producto : productos) {

        out.printf("""
                                <option value="%d">
                                    %s - S/ %s - stock: %d
                                </option>
                                %n
                        """,
                producto.getId(),
                escapeHtml(
                        producto.getNombre()
                ),
                producto.getPrecio()
                        .toPlainString(),
                producto.getStock()
        );
      }

      out.println("""
                            </select>
                            </label>

                            <label>
                                Cantidad

                                <input
                                    name="cantidad"
                                    type="number"
                                    min="1"
                                    value="1"
                                    required>
                            </label>

                            <button
                                type="submit"
                                class="btn-registrar">
                                Registrar
                            </button>

                        </form>

                        <section
                            id="formEdicion"
                            class="edit-panel"
                            style="display:none;">

                            <h2>
                                Editar pedido #
                                <span id="editPedidoIdTexto"></span>
                            </h2>

                            <p class="nota">
                                Modifica los datos del pedido seleccionado.
                                Los demás pedidos no serán afectados.
                            </p>

                            <form id="formEditar">

                                <input
                                    type="hidden"
                                    id="editPedidoId">

                                <label>
                                    Cliente

                                    <input
                                        type="text"
                                        id="editCliente"
                                        maxlength="120"
                                        required>
                                </label>

                                <label>
                                    Producto

                                    <select
                                        id="editProducto"
                                        required>
                    """);

      for (Producto producto : productos) {

        out.printf("""
                                    <option value="%d">
                                        %s - S/ %s - stock: %d
                                    </option>
                                    %n
                            """,
                producto.getId(),
                escapeHtml(
                        producto.getNombre()
                ),
                producto.getPrecio()
                        .toPlainString(),
                producto.getStock()
        );
      }

      out.println("""
                                </select>
                                </label>

                                <label>
                                    Cantidad

                                    <input
                                        type="number"
                                        id="editCantidad"
                                        min="1"
                                        required>
                                </label>

                                <div class="edit-actions">

                                    <button
                                        type="submit"
                                        class="btn-guardar">
                                        Guardar cambios
                                    </button>

                                    <button
                                        type="button"
                                        class="btn-cancelar"
                                        onclick="cancelarEdicion()">
                                        Cancelar
                                    </button>

                                </div>

                            </form>

                        </section>

                        <h2>Pedidos registrados</h2>

                        <table>

                            <thead>

                                <tr>
                                    <th>ID</th>
                                    <th>Cliente</th>
                                    <th>Producto</th>
                                    <th>Cantidad</th>
                                    <th>Total</th>
                                    <th>Fecha</th>
                                    <th>Acciones</th>
                                </tr>

                            </thead>

                            <tbody>
                    """);

      DateTimeFormatter formatter =
              DateTimeFormatter.ofPattern(
                      "dd/MM/yyyy HH:mm:ss"
              );

      for (Pedido pedido : pedidos) {

        out.printf("""
                            <tr>

                                <td>%d</td>

                                <td>%s</td>

                                <td>%s</td>

                                <td>%d</td>

                                <td>S/ %s</td>

                                <td>%s</td>

                                <td class="acciones">

                                    <button
                                        type="button"
                                        class="btn-editar"
                                        onclick="editarPedido(this)"
                                        data-id="%d"
                                        data-cliente="%s"
                                        data-producto="%d"
                                        data-cantidad="%d">
                                        Editar
                                    </button>

                                    <button
                                        type="button"
                                        class="btn-eliminar"
                                        onclick="eliminarPedido(%d)">
                                        Eliminar
                                    </button>

                                </td>

                            </tr>
                            %n
                        """,
                pedido.getId(),

                escapeHtml(
                        pedido.getCliente()
                ),

                escapeHtml(
                        pedido.getProducto()
                                .getNombre()
                ),

                pedido.getCantidad(),

                pedido.getTotal()
                        .toPlainString(),

                pedido.getFecha()
                        .format(formatter),

                pedido.getId(),

                escapeHtml(
                        pedido.getCliente()
                ),

                pedido.getProducto()
                        .getId(),

                pedido.getCantidad(),

                pedido.getId()
        );
      }

      if (pedidos.isEmpty()) {

        out.println("""
                            <tr>
                                <td colspan="7">
                                    Aún no hay pedidos.
                                </td>
                            </tr>
                        """);
      }

      out.println("""
                            </tbody>

                        </table>

                        <script>

                            function editarPedido(boton) {

                                const id =
                                    boton.dataset.id;

                                const cliente =
                                    boton.dataset.cliente;

                                const productoId =
                                    boton.dataset.producto;

                                const cantidad =
                                    boton.dataset.cantidad;

                                document.getElementById(
                                    "editPedidoId"
                                ).value = id;

                                document.getElementById(
                                    "editPedidoIdTexto"
                                ).textContent = id;

                                document.getElementById(
                                    "editCliente"
                                ).value = cliente;

                                document.getElementById(
                                    "editProducto"
                                ).value = productoId;

                                document.getElementById(
                                    "editCantidad"
                                ).value = cantidad;

                                document.getElementById(
                                    "formEdicion"
                                ).style.display = "block";

                                document.getElementById(
                                    "formEdicion"
                                ).scrollIntoView({
                                    behavior: "smooth"
                                });
                            }

                            function cancelarEdicion() {

                                document.getElementById(
                                    "formEdicion"
                                ).style.display = "none";

                                document.getElementById(
                                    "formEditar"
                                ).reset();
                            }

                            document.getElementById(
                                "formEditar"
                            ).addEventListener(
                                "submit",
                                function(event) {

                                    event.preventDefault();

                                    const id =
                                        document.getElementById(
                                            "editPedidoId"
                                        ).value;

                                    const cliente =
                                        document.getElementById(
                                            "editCliente"
                                        ).value.trim();

                                    const productoId =
                                        document.getElementById(
                                            "editProducto"
                                        ).value;

                                    const cantidad =
                                        document.getElementById(
                                            "editCantidad"
                                        ).value;

                                    if (!cliente) {

                                        alert(
                                            "El cliente es obligatorio."
                                        );

                                        return;
                                    }

                                    if (!productoId) {

                                        alert(
                                            "Debe seleccionar un producto."
                                        );

                                        return;
                                    }

                                    if (!cantidad ||
                                        Number(cantidad) <= 0) {

                                        alert(
                                            "La cantidad debe ser mayor que cero."
                                        );

                                        return;
                                    }

                                    const confirmar =
                                        confirm(
                                            "¿Deseas guardar los cambios " +
                                            "del pedido " + id + "?"
                                        );

                                    if (!confirmar) {
                                        return;
                                    }

                                    const url =
                                        "pedidos?id="
                                        + encodeURIComponent(id)
                                        + "&cliente="
                                        + encodeURIComponent(cliente)
                                        + "&productoId="
                                        + encodeURIComponent(productoId)
                                        + "&cantidad="
                                        + encodeURIComponent(cantidad);

                                    fetch(
                                        url,
                                        {
                                            method: "PUT"
                                        }
                                    )
                                    .then(
                                        async function(response) {

                                            const mensaje =
                                                await response.text();

                                            if (!response.ok) {

                                                throw new Error(
                                                    mensaje
                                                );
                                            }

                                            /*
                                             * Mostrar primero la respuesta
                                             * del servidor.
                                             */
                                            alert(mensaje);

                                            /*
                                             * Recargar la página enviando
                                             * el ID actualizado.
                                             *
                                             * De esta manera doGet()
                                             * mostrará:
                                             *
                                             * Pedido actualizado
                                             * correctamente. ID: X
                                             */
                                            window.location.href =
                                                "pedidos?actualizado="
                                                + encodeURIComponent(id);
                                        }
                                    )
                                    .catch(
                                        function(error) {

                                            alert(
                                                "Error al actualizar: "
                                                + error.message
                                            );
                                        }
                                    );

                                }
                            );

                            function eliminarPedido(id) {

                                const confirmar =
                                    confirm(
                                        "¿Deseas eliminar el pedido "
                                        + id
                                        + "?"
                                    );

                                if (!confirmar) {
                                    return;
                                }

                                fetch(
                                    "pedidos?id="
                                    + encodeURIComponent(id),
                                    {
                                        method: "DELETE"
                                    }
                                )
                                .then(
                                    async function(response) {

                                        if (!response.ok) {

                                            const mensaje =
                                                await response.text();

                                            throw new Error(
                                                mensaje
                                            );
                                        }

                                        /*
                                         * DELETE responde 204,
                                         * por lo que no se necesita
                                         * leer response.text().
                                         */
                                        window.location.href =
                                            "pedidos?eliminado="
                                            + encodeURIComponent(id);
                                    }
                                )
                                .catch(
                                    function(error) {

                                        alert(
                                            "Error al eliminar: "
                                            + error.message
                                        );
                                    }
                                );
                            }

                        </script>

                    </body>

                    </html>
                    """);
    }
  }

  private String escapeHtml(String value) {

    if (value == null) {
      return "";
    }

    return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
  }
}
