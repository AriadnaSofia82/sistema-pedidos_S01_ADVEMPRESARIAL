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

    renderizarPagina(response, null);
  }
  @Override
  protected void doPost(
          HttpServletRequest request,
          HttpServletResponse response)
          throws ServletException, IOException {

    request.setCharacterEncoding(StandardCharsets.UTF_8.name());

    try {
      String cliente = request.getParameter("cliente");

      Long productoId = Long.valueOf(
              request.getParameter("productoId")
      );

      int cantidad = Integer.parseInt(
              request.getParameter("cantidad")
      );

      Pedido pedido = pedidoService.registrarPedido(
              cliente,
              productoId,
              cantidad
      );
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
              "Producto o cantidad inválidos."
      );

    } catch (IllegalArgumentException |
             IllegalStateException e) {

      response.setStatus(
              HttpServletResponse.SC_BAD_REQUEST
      );

      renderizarPagina(
              response,
              e.getMessage()
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
      Long id = Long.valueOf(
              request.getParameter("id")
      );

      String cliente = request.getParameter("cliente");

      Long productoId = Long.valueOf(
              request.getParameter("productoId")
      );

      int cantidad = Integer.parseInt(
              request.getParameter("cantidad")
      );

      Pedido pedido = pedidoService.actualizarPedido(
              id,
              cliente,
              productoId,
              cantidad
      );

      response.setContentType(
              "text/plain;charset=UTF-8"
      );

      response.setStatus(
              HttpServletResponse.SC_OK
      );

      try (PrintWriter out = response.getWriter()) {
        out.println(
                "Pedido actualizado correctamente. ID: "
                        + pedido.getId()
        );
      }

    } catch (NumberFormatException e) {

      response.setStatus(
              HttpServletResponse.SC_BAD_REQUEST
      );

      response.setContentType(
              "text/plain;charset=UTF-8"
      );

      response.getWriter().println(
              "ID, producto o cantidad inválidos."
      );

    } catch (IllegalArgumentException |
             IllegalStateException e) {

      response.setStatus(
              HttpServletResponse.SC_BAD_REQUEST
      );

      response.setContentType(
              "text/plain;charset=UTF-8"
      );

      response.getWriter().println(
              e.getMessage()
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
      Long id = Long.valueOf(
              request.getParameter("id")
      );

      pedidoService.eliminarPedido(id);

      response.setStatus(
              HttpServletResponse.SC_NO_CONTENT
      );

    } catch (NumberFormatException e) {

      response.setStatus(
              HttpServletResponse.SC_BAD_REQUEST
      );

      response.setContentType(
              "text/plain;charset=UTF-8"
      );

      response.getWriter().println(
              "El ID del pedido es inválido."
      );

    } catch (IllegalArgumentException |
             IllegalStateException e) {

      response.setStatus(
              HttpServletResponse.SC_BAD_REQUEST
      );

      response.setContentType(
              "text/plain;charset=UTF-8"
      );

      response.getWriter().println(
              e.getMessage()
      );
    }
  }

  private void renderizarPagina(
          HttpServletResponse response,
          String error)
          throws IOException {

    List<Producto> productos =
            pedidoService.listarProductos();

    List<Pedido> pedidos =
            pedidoService.listarPedidos();

    response.setContentType(
            "text/html;charset=UTF-8"
    );

    try (PrintWriter out = response.getWriter()) {

      out.println("""
                    <!doctype html>
                    <html lang="es">
                    <head>
                      <meta charset="UTF-8">
                      <meta name="viewport"
                            content="width=device-width, initial-scale=1">

                      <title>Sistema de Pedidos - ISIL</title>

                      <style>
                        body {
                          font-family: Arial, sans-serif;
                          max-width: 1100px;
                          margin: 32px auto;
                          padding: 0 16px;
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
                        }

                        button {
                          cursor: pointer;
                        }

                        table {
                          width: 100%;
                          border-collapse: collapse;
                          margin-top: 24px;
                        }

                        th,
                        td {
                          border: 1px solid #ccc;
                          padding: 9px;
                          text-align: left;
                        }

                        th {
                          background: #f2f2f2;
                        }

                        .error {
                          background: #ffe7e7;
                          border: 1px solid #d33;
                          padding: 10px;
                          margin: 16px 0;
                        }

                        .success {
                          background: #e7ffe7;
                          border: 1px solid #3a3;
                          padding: 10px;
                          margin: 16px 0;
                        }

                        .nota {
                          background: #f5f5f5;
                          padding: 10px;
                          margin: 16px 0;
                        }

                        .acciones {
                          white-space: nowrap;
                        }

                        .btn-editar {
                          background: #f0ad4e;
                          border: none;
                          color: white;
                        }

                        .btn-eliminar {
                          background: #d9534f;
                          border: none;
                          color: white;
                        }

                        @media (max-width: 800px) {
                          form {
                            grid-template-columns: 1fr;
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
                        H2.
                      </p>
                    """);

      if (error != null) {

        out.printf(
                "<div class=\"error\">%s</div>%n",
                escapeHtml(error)
        );
      }

      String creado =
              requestParameterSeguro(
                      response,
                      "creado"
              );

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

        out.printf(
                """
                <option value="%d">
                  %s - S/ %s - stock: %d
                </option>
                %n
                """,
                producto.getId(),
                escapeHtml(producto.getNombre()),
                producto.getPrecio().toPlainString(),
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

                        <button type="submit">
                          Registrar
                        </button>

                      </form>

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

        out.printf(
                """
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
                      onclick="editarPedido(%d)">
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
                escapeHtml(pedido.getCliente()),
                escapeHtml(
                        pedido.getProducto().getNombre()
                ),
                pedido.getCantidad(),
                pedido.getTotal().toPlainString(),
                pedido.getFecha().format(formatter),
                pedido.getId(),
                pedido.getId()
        );
      }

      if (pedidos.isEmpty()) {

        out.println(
                """
                <tr>
                  <td colspan="7">
                    Aún no hay pedidos.
                  </td>
                </tr>
                """
        );
      }

      out.println("""
                        </tbody>
                      </table>

                      <script>

                        function editarPedido(id) {

                          const cliente =
                            prompt(
                              "Nuevo cliente:"
                            );

                          if (cliente === null) {
                            return;
                          }

                          const productoId =
                            prompt(
                              "Nuevo ID de producto:"
                            );

                          if (productoId === null) {
                            return;
                          }

                          const cantidad =
                            prompt(
                              "Nueva cantidad:"
                            );

                          if (cantidad === null) {
                            return;
                          }

                          fetch(
                            "pedidos?id="
                              + encodeURIComponent(id)
                              + "&cliente="
                              + encodeURIComponent(cliente)
                              + "&productoId="
                              + encodeURIComponent(productoId)
                              + "&cantidad="
                              + encodeURIComponent(cantidad),
                            {
                              method: "PUT"
                            }
                          )
                          .then(async response => {

                            const mensaje =
                              await response.text();

                            if (!response.ok) {
                              throw new Error(
                                mensaje
                              );
                            }

                            alert(mensaje);

                            location.reload();
                          })
                          .catch(error => {

                            alert(
                              "Error: "
                                + error.message
                            );
                          });
                        }


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
                          .then(async response => {

                            if (!response.ok) {

                              const mensaje =
                                await response.text();

                              throw new Error(
                                mensaje
                              );
                            }

                            alert(
                              "Pedido eliminado correctamente."
                            );

                            location.reload();
                          })
                          .catch(error => {

                            alert(
                              "Error: "
                                + error.message
                            );
                          });
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

  private String requestParameterSeguro(
          HttpServletResponse response,
          String parametro) {

    return null;
  }
}
