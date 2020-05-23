package in.lubble.app.utils;

import java.io.Serializable;

public interface CompleteListener extends Serializable {
    // serialVersionUid is NOT needed for interfaces
    void onComplete(boolean isSuccess);

}
