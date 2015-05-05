import java.lang.Object;
import java.lang.Override;

/**
 * A simple class with Javadoc.
 *
 * @since 1.0.0
 */
public class Javadoc {
  private final String name;

  private final int count;

  public Javadoc(String name, int count) {
    this.name = name;
    this.count = count;
  }

  /**
   * Returns the name.
   *
   * @return the name.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the count.
   *
   * @return the count.
   */
  public int getCount() {
    return count;
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + (name == null ? 0 : name.hashCode());
    result = prime * result + count;
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this)
        return true;
    else if (other == null || getClass() != other.getClass())
        return false;
    Javadoc that = (Javadoc) other;
    return equals(name, that.name)
            && count == that.count;
  }

  private boolean equals(Object left, Object right) {
    if (left == null)
        return right == null;
    else
        return left.equals(right);
  }
}
