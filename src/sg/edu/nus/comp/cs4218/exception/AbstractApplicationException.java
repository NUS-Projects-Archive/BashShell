package sg.edu.nus.comp.cs4218.exception;

import java.util.List;
import java.util.stream.Collectors;

import sg.edu.nus.comp.cs4218.impl.util.CollectionsUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

public abstract class AbstractApplicationException extends Exception {

    private static final long serialVersionUID = -6276854591710517685L;

    public AbstractApplicationException(String message) {
        super(message);
    }

    public AbstractApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public <T extends AbstractApplicationException> AbstractApplicationException(List<T> exceptions) {
        this(String.join(StringUtils.STRING_NEWLINE,
                CollectionsUtils.listToArray(
                        exceptionListToMessageList(exceptions)
                )
        ));
    }

    private static <T extends Exception> List<String> exceptionListToMessageList(List<T> exceptions) {
        return exceptions.stream().map(Throwable::getMessage).collect(Collectors.toList());
    }
}
