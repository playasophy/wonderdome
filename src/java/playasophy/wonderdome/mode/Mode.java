package playasophy.wonderdome.mode;

import clojure.lang.Associative;

public interface Mode<T> {

    T update(Associative event);

    int render(Associative pixel);

}
