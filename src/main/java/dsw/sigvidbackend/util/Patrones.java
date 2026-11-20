package dsw.sigvidbackend.util;

// expresiones regulares compartidas por las validaciones de los DTOs (@Pattern)
public final class Patrones {

    private Patrones() {
    }

    // nombres y apellidos: letras (con tildes y ñ), espacios, apostrofe y guion, de 2 a 50
    public static final String NOMBRE = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü' -]{2,50}$";
    // DNI peruano: 8 digitos
    public static final String DNI = "^\\d{8}$";
    // RUC peruano: 11 digitos que empiezan en 10, 15, 17 o 20
    public static final String RUC = "^(10|15|17|20)\\d{9}$";
    // telefono: 7 a 15 digitos
    public static final String TELEFONO = "^\\d{7,15}$";
    // codigo de producto: 3 letras mayusculas, guion y 3 digitos (ej. VIN-001)
    public static final String CODIGO_PRODUCTO = "^[A-Z]{3}-\\d{3}$";
    // tipo de descuento del pedido
    public static final String TIPO_DESCUENTO = "^(?i)(PORCENTAJE|MONTO_FIJO)$";
    // metodo de pago
    public static final String METODO_PAGO = "^(?i)(Efectivo|Tarjeta|Transferencia)$";
    // codigo de voucher / operacion: letras, numeros y guiones
    public static final String CODIGO_VOUCHER = "^[A-Za-z0-9-]{4,30}$";
    // texto general (direcciones, razon social): sin < > para evitar inyeccion html
    public static final String TEXTO_SEGURO = "^[^<>]*$";
    // documento del cliente en el pedido: DNI (8 digitos) o RUC (11 digitos)
    public static final String DNI_O_RUC = "^(\\d{8}|(10|15|17|20)\\d{9})$";
    // voucher digital adjunto (HU05): data URL png/jpeg/pdf en base64
    public static final String VOUCHER_ARCHIVO = "^data:(image/png|image/jpeg|application/pdf);base64,[A-Za-z0-9+/=]+$";
    // tipo de confirmacion de recepcion (HU10)
    public static final String TIPO_RECEPCION = "^(CONFORMIDAD|FIRMA)$";
    // firma digital dibujada en canvas: data URL png en base64
    public static final String FIRMA_PNG = "^data:image/png;base64,[A-Za-z0-9+/=]+$";
    // busqueda libre (codigo, nombre o categoria): letras, numeros, espacios y guiones, max 50
    public static final String BUSQUEDA = "^[A-Za-z0-9ÁÉÍÓÚáéíóúÑñÜü' -]{0,50}$";
}
