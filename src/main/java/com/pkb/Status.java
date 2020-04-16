package com.pkb;

import java.util.stream.Stream;

import static java.lang.String.format;

public enum Status {
    open(2),
    pending(3),
    resolved(4),
    closed(5),
    waiting_on_customer(6),
    waiting_on_3rd_party(7),
    waiting_on_developer(9),
    waiting_on_senior_stuff_input(11),
    waiting_on_jira(12),
    cie_deletion(16);

    public final int code;

    Status(int code) {
        this.code = code;
    }

    static Status fromCode(int code) {
        return Stream.of(Status.values())
                .filter(status -> code == status.code)
                .findFirst().orElseThrow(() -> new IllegalStateException(format("%s is not recognized status code", code)));
    }
}
