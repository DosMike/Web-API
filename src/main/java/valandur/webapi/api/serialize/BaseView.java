package valandur.webapi.api.serialize;

public class BaseView<T> {

    protected T value;

    public BaseView(T value) {
        this.value = value;
    }
}
