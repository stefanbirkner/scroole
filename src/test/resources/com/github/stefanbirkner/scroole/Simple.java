import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.util.List;
import java.util.Set;

public class Simple {
  private final String name;

  private final int count;

  private final Simple[] children;

  private final Set<List<Integer>> numbers;

  public Simple(String name, int count, Simple[] children, Set<List<Integer>> numbers) {
    this.name = name;
    this.count = count;
    this.children = children;
    this.numbers = numbers;
  }

  public String getName() {
    return name;
  }

  public int getCount() {
    return count;
  }

  public Simple[] getChildren() {
    return children;
  }

  public Set<List<Integer>> getNumbers() {
    return numbers;
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + (name == null ? 0 : name.hashCode());
    result = prime * result + count;
    result = prime * result + java.util.Arrays.hashCode(children);
    result = prime * result + (numbers == null ? 0 : numbers.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this)
        return true;
    else if (other == null || getClass() != other.getClass())
        return false;
    Simple that = (Simple) other;
    return equals(name, that.name)
            && count == that.count
            && java.util.Arrays.equals(children, that.children)
            && equals(numbers, that.numbers);
  }

  private boolean equals(Object left, Object right) {
    if (left == null)
        return right == null;
    else
        return left.equals(right);
  }
}
