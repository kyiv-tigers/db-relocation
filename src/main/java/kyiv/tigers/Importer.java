package kyiv.tigers;

/**
 * @author Roman Malyarchuk
 * @project db-relocation
 * @since 08/02/20 14:09
 */
public interface Importer {
    Importer setNext(Importer next);
    boolean start();
}
