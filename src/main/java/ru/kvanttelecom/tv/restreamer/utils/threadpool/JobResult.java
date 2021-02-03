package ru.kvanttelecom.tv.restreamer.utils.threadpool;

import lombok.Data;

@Data
public class JobResult<A, R> {

    // argument with whom job was called (identity purposes)
    private A argument;
    // JOb result (null if job exceptionally failed)
    private R result;

    // Job fail exception (null if was executed successfully)
    private Throwable exception;

    public JobResult() {}

    public JobResult(A argument) {
        this.argument = argument;
    }

    public JobResult(A argument, R result) {
        this.argument = argument;
        this.result = result;
    }

    @Override
    public String toString() {
        String result = "";
        if (exception != null) {
            result += ", exception= " + exception.toString();
        }
        return result;
    }
}
