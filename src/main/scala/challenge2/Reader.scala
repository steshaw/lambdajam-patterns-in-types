package challenge2

import challenge0._

/*
 * A reader data type that represents the application of some
 * environment to produce a value.
 */
case class Reader[R, A](run: R => A) {

  /*
   * Exercise 2.1:
   *
   * Implement map for Reader[R, A].
   *
   * The following laws must hold:
   *  1) r.map(z => z) == r
   *  2) r.map(z => f(g(z))) == r.map(g).map(f)
   */
  def map[B](f: A => B): Reader[R, B] = Reader(r => f(run(r)))

  /*
   * Exercise 2.2:
   *
   * Implement flatMap (a.k.a. bind, a.k.a. >>=).
   *
   * The following law must hold:
   *   r.flatMap(f).flatMap(g) == r.flatMap(z => f(z).flatMap(g))
   */
  def flatMap[B](f: A => Reader[R, B]): Reader[R, B] =
    Reader(r => f(this.run(r)).run(r))
}

object Reader {
  /*
   * Exercise 2.3:
   *
   * Implement value  (a.k.a. return, point, pure).
   *
   * Hint: Try using Reader constructor.
   */
  def value[R, A](a: => A): Reader[R, A] = Reader(r => a)

  /*
   * Exercise 2.4:
   *
   * Implement ask.
   *
   * Ask provides access to the current environment (R).
   *
   * Hint: Try using Reader constructor.
   */
  def ask[R]: Reader[R, R] = Reader(identity)

  /*
   * Exercise 2.5:
   *
   * Implement local.
   *
   * Local produce a reader that runs with a modified environment.
   *
   * Hint: Try using Reader constructor.
   */
  def local[R, A](f: R => R)(reader: Reader[R, A]): Reader[R, A] =
    Reader(run = r => reader.run(f(r)))

  /** see aside below, this is a convenience type for partially applying R to Reader[R, A] */
  class Reader_[R] {
    type l[a] = Reader[R, a]
  }

  implicit def ReaderMonad[R]: Monad[Reader_[R]#l] =
    new Monad[Reader_[R]#l] {
      def point[A](a: => A): Reader[R, A] =
        value(a)

      def bind[A, B](r: Reader[R, A])(f: A => Reader[R, B]) =
        r flatMap f
    }

  /*
   * Exercise 2.6:
   *
   * Implement monoid type class for Reader[R, A], given a monoid for A.
   */
  implicit def ReaderMonoid[R, A: Monoid]: Monoid[Reader[R, A]] =
    new Monoid[Reader[R, A]] {
      def zero: Reader[R, A] = Reader(r => implicitly[Monoid[A]].zero)

      def append(a: Reader[R, A], b: => Reader[R, A]) =
        // Reader(r => implicitly[Monoid[A]].append(a.run(r), b.run(r)))
        Reader(r => Monoid[A].append(a.run(r), b.run(r)))
    }

  // Aside: You may be unfamliar with the type trick above, Reader_[R]#l. but
  //        may have seen its more common (inline) form, referred to as a
  //        'type lambda', i.e.
  //           ({type λ[α] = Reader[R, α]})#λ
  //
  //        What this does (in both examples) is use path dependent types to
  //        express partial application of a type constructor. In the example
  //        Reader is a type constructor that takes two arguments, R and A, but we want
  //        to refer to the monad, Reader[R, _] that only has a single type
  //        argument. The 'type lambda' version creates an anonymous structural
  //        type, the long form uses a standard named class.
}
