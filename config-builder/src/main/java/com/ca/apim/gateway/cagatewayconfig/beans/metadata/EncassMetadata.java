package com.ca.apim.gateway.cagatewayconfig.beans.metadata;

import com.ca.apim.gateway.cagatewayconfig.beans.EncassArgument;
import com.ca.apim.gateway.cagatewayconfig.beans.EncassResult;
import com.ca.apim.gateway.cagatewayconfig.util.entity.EntityTypes;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.LinkedHashSet;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EncassMetadata extends MetadataBase {
    private Set<EncassArgument> arguments;
    private Set<EncassResult> results;

    private EncassMetadata(final String name) {
        super(EntityTypes.ENCAPSULATED_ASSERTION_TYPE, name);
    }

    public Set<EncassArgument> getArguments() {
        return arguments;
    }

    public Set<EncassResult> getResults() {
        return results;
    }

    public static class Builder {
        private final String name;
        private final Set<EncassArgument> arguments = new LinkedHashSet<>();
        private final Set<EncassResult> results = new LinkedHashSet<>();

        public Builder(final String name) {
            this.name = name;
        }

        public Builder arguments(final Set<EncassArgument> arguments) {
            this.arguments.clear();
            this.arguments.addAll(arguments);
            return this;
        }

        public Builder results(final Set<EncassResult> results) {
            this.results.clear();
            this.results.addAll(results);
            return this;
        }

        public EncassMetadata build() {
            EncassMetadata encassMetadata = new EncassMetadata(name);
            if (!arguments.isEmpty()) {
                encassMetadata.arguments = arguments;
            }
            if (!results.isEmpty()) {
                encassMetadata.results = results;
            }
            return encassMetadata;
        }
    }
}
