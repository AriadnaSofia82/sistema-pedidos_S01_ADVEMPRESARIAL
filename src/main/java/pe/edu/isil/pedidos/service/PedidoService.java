package pe.edu.isil.pedidos.service;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import pe.edu.isil.pedidos.domain.Pedido;
import pe.edu.isil.pedidos.domain.Producto;

import java.math.BigDecimal;
import java.util.List;

@Stateless
public class PedidoService {

    @PersistenceContext(unitName = "PedidosPU")
    private EntityManager entityManager;
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Pedido registrarPedido(
            String cliente,
            Long productoId,
            int cantidad) {

        if (cliente == null || cliente.isBlank()) {
            throw new IllegalArgumentException(
                    "El cliente es obligatorio."
            );
        }

        if (productoId == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un producto."
            );
        }

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero."
            );
        }

        Producto producto =
                entityManager.find(
                        Producto.class,
                        productoId
                );

        if (producto == null) {
            throw new IllegalArgumentException(
                    "El producto no existe."
            );
        }
        producto.descontarStock(cantidad);

        BigDecimal total =
                producto.getPrecio()
                        .multiply(
                                BigDecimal.valueOf(cantidad)
                        );

        Pedido pedido =
                new Pedido(
                        cliente.trim(),
                        producto,
                        cantidad,
                        total
                );

        entityManager.persist(pedido);

        return pedido;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Pedido actualizarPedido(
            Long pedidoId,
            String cliente,
            Long productoId,
            Integer cantidad) {
        if (pedidoId == null) {
            throw new IllegalArgumentException(
                    "El ID del pedido es obligatorio."
            );
        }
        Pedido pedido =
                entityManager.find(
                        Pedido.class,
                        pedidoId
                );

        if (pedido == null) {
            throw new IllegalArgumentException(
                    "El pedido no existe."
            );
        }
        String clienteActual =
                pedido.getCliente();

        Producto productoActual =
                pedido.getProducto();

        int cantidadActual =
                pedido.getCantidad();
        String clienteNuevo;

        if (cliente == null) {

            clienteNuevo =
                    clienteActual;

        } else {

            if (cliente.isBlank()) {
                throw new IllegalArgumentException(
                        "El cliente no puede estar vacío."
                );
            }

            clienteNuevo =
                    cliente.trim();
        }
        Producto productoNuevo;

        if (productoId == null) {

            productoNuevo =
                    productoActual;

        } else {

            productoNuevo =
                    entityManager.find(
                            Producto.class,
                            productoId
                    );

            if (productoNuevo == null) {
                throw new IllegalArgumentException(
                        "El producto no existe."
                );
            }
        }
        int cantidadNueva;

        if (cantidad == null) {

            cantidadNueva =
                    cantidadActual;

        } else {

            if (cantidad <= 0) {
                throw new IllegalArgumentException(
                        "La cantidad debe ser mayor que cero."
                );
            }

            cantidadNueva =
                    cantidad;
        }

        if (productoActual.getId().equals(
                productoNuevo.getId())) {

            int diferencia =
                    cantidadNueva - cantidadActual;
            if (diferencia > 0) {

                try {

                    productoNuevo.descontarStock(
                            diferencia
                    );

                } catch (IllegalStateException e) {

                    throw new IllegalStateException(
                            "Stock insuficiente. "
                                    + e.getMessage()
                    );
                }
            }
            else if (diferencia < 0) {

                productoNuevo.aumentarStock(
                        -diferencia
                );
            }
        }

        else {
            if (cantidadNueva >
                    productoNuevo.getStock()) {

                throw new IllegalStateException(
                        "Stock insuficiente para el nuevo "
                                + "producto. Disponible: "
                                + productoNuevo.getStock()
                );
            }
            productoActual.aumentarStock(
                    cantidadActual
            );

            productoNuevo.descontarStock(
                    cantidadNueva
            );
        }

        BigDecimal totalNuevo =
                productoNuevo.getPrecio()
                        .multiply(
                                BigDecimal.valueOf(
                                        cantidadNueva
                                )
                        );
        pedido.actualizar(
                clienteNuevo,
                productoNuevo,
                cantidadNueva,
                totalNuevo
        );

        return pedido;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void eliminarPedido(
            Long pedidoId) {

        if (pedidoId == null) {
            throw new IllegalArgumentException(
                    "El ID del pedido es obligatorio."
            );
        }

        Pedido pedido =
                entityManager.find(
                        Pedido.class,
                        pedidoId
                );

        if (pedido == null) {
            throw new IllegalArgumentException(
                    "El pedido no existe."
            );
        }

        Producto producto =
                pedido.getProducto();
        producto.aumentarStock(
                pedido.getCantidad()
        );
        entityManager.remove(pedido);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public List<Producto> listarProductos() {

        inicializarProductosSiEsNecesario();

        return entityManager.createQuery(
                "select p from Producto p order by p.id",
                Producto.class
        ).getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Pedido> listarPedidos() {
        return entityManager.createQuery(
                "select p from Pedido p " +
                        "join fetch p.producto " +
                        "order by p.id desc",
                Pedido.class
        ).getResultList();
    }
    private void inicializarProductosSiEsNecesario() {

        Long cantidad =
                entityManager.createQuery(
                        "select count(p) from Producto p",
                        Long.class
                ).getSingleResult();

        if (cantidad == 0) {

            entityManager.persist(
                    new Producto(
                            "Laptop",
                            new BigDecimal("2500.00"),
                            5
                    )
            );

            entityManager.persist(
                    new Producto(
                            "Monitor",
                            new BigDecimal("850.00"),
                            8
                    )
            );

            entityManager.persist(
                    new Producto(
                            "Teclado",
                            new BigDecimal("120.00"),
                            15
                    )
            );
        }
    }
}