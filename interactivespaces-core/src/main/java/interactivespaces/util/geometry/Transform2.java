/**
 *
 */
package interactivespaces.util.geometry;

import java.util.List;

/**
 * @author Keith M. Hughes
 */
public class Transform2 {

  /**
   * The transform matrix.
   */
  private final Matrix3 transform;

  /**
   * A temporary matrix for composing transforms.
   */
  private final Matrix3 temp;

  /**
   * Construct the transform currently as an identity transform.
   */
  public Transform2() {
    transform = new Matrix3().identity();

    temp = new Matrix3();
  }

  /**
   * Transform the given vector according to the current transform.
   *
   * @param v
   *          the given vector
   *
   * @return a newly constructed vector transformed
   */
  public Vector2 transform(Vector2 v) {
    return v.multiply(transform);
  }

  /**
   * Transform the given vector according to the current transform.
   *
   * @param v
   *          the given vector
   *
   * @return the given vector with its coordinates transformed
   */
  public Vector2 transformSelf(Vector2 v) {
    return v.multiplySelf(transform);
  }

  /**
   * Transform the given list of vectors according to the current transform.
   *
   * <p>
   * The original list of the original vectors is returned with just a new set
   * of coordinates in the vectors.
   *
   * @param vectors
   *          the given vectors
   *
   * @return this transform
   */
  public Transform2 transformSelf(List<Vector2> vectors) {
    for (Vector2 v : vectors) {
      transformSelf(v);
    }

    return this;
  }

  /**
   * Reset the transform to an identify transform.
   *
   * @return this transform
   */
  public Transform2 reset() {
    identity();

    return this;
  }

  /**
   * Multiply the transform by the supplied matrix.
   *
   * @param m
   *          the supplied matrix
   *
   * @return this transform
   */
  public Transform2 multiply(Matrix3 m) {
    transform.multiplySelf(m);

    return this;
  }

  /**
   * Set the transform to the supplied matrix.
   *
   * @param m
   *          the supplied matrix
   *
   * @return this transform
   */
  public Transform2 set(Matrix3 m) {
    transform.set(m);

    return this;
  }

  /**
   * Get the current transform matrix.
   *
   * @return the current transform matrix
   */
  public Matrix3 get() {
    return transform;
  }

  /**
   * Make the current transform the identity transform.
   *
   * @return this transform
   */
  public Transform2 identity() {
    transform.identity();

    return this;
  }

  /**
   * Translate by a given set of coordinates.
   *
   * @param tx
   *          the amount along the x axis
   * @param ty
   *          the amount along the y axis
   *
   * @return this transform
   */
  public Transform2 translate(double tx, double ty) {
    temp.identity().setEntry(0, 2, tx).setEntry(1, 2, ty);

    return multiply(temp);
  }

  /**
   * Scale the current transform by each of the following scales.
   *
   * @param sx
   *          the scale in x
   * @param sy
   *          the scale in y
   *
   * @return this transform
   */
  public Transform2 scale(double sx, double sy) {
    temp.identity().setEntry(0, 0, sx).setEntry(1, 1, sy);

    return multiply(temp);
  }

  /**
   * Scale the transform by the corresponding components of the vector.
   *
   * @param s
   *          the scaling vector
   *
   * @return this transform
   */
  public Transform2 scale(Vector2 s) {
    return scale(s.getV0(), s.getV1());
  }

  /**
   * Translate the current transform by the scaled vector.
   *
   * @param v
   *          the base vector for the translation
   * @param scale
   *          the factor by which the vector will be scaled
   *
   * @return this transform
   */
  public Transform2 translate(Vector2 v, double scale) {
    return translate(scale * v.getV0(), scale * v.getV1());
  }

  /**
   * Translate the transform by the supplied vector.
   *
   * @param v
   *          the vector by which to translate
   *
   * @return this transform
   */
  public Transform2 translate(Vector2 v) {
    return translate(v.getV0(), v.getV1());
  }

  /**
   * Rotate the transform by the supplied angle.
   *
   * <p>
   * Positive values of rotation will rotate from the positive x axis to the
   * positive y axis.
   *
   * @param angle
   *          the angle, in radians
   *
   * @return this transform
   */
  public Transform2 rotate(double angle) {
    temp.identity().setEntry(0, 0, Math.cos(angle)).setEntry(0, 1, -Math.sin(angle)).setEntry(1, 0, Math.sin(angle))
        .setEntry(1, 1, Math.cos(angle));

    return multiply(temp);
  }
}
