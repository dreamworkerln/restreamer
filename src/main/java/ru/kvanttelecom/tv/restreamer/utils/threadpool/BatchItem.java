package ru.kvanttelecom.tv.restreamer.utils.threadpool;

import lombok.Data;

import java.util.function.Function;

@Data
public class BatchItem<A, R> {

    // Job argument
    private A argument;

    // Function<> a -> function(a), function that do the job
    private Function<A, JobResult<A,R>> function;

    public BatchItem(A argument, Function<A, JobResult<A, R>> function) {
        this.argument = argument;
        this.function = function;
    }
}
