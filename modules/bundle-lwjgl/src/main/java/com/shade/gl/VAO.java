package com.shade.gl;

import com.shade.util.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

public class VAO implements GLObject<VAO> {
    private final int id;
    private final List<VBO> vbos;

    public VAO() {
        this.id = glGenVertexArrays();
        this.vbos = new ArrayList<>(1);
        bind();
    }

    @NotNull
    public VBO createBuffer(@NotNull Attribute... attributes) {
        return createBuffer(Arrays.asList(attributes));
    }

    @NotNull
    public VBO createBuffer(@NotNull Collection<Attribute> attributes) {
        final VBO vbo = new VBO(GL_ARRAY_BUFFER, GL_STATIC_DRAW, attributes);
        vbos.add(vbo);
        return vbo;
    }

    @NotNull
    public VBO createIndexBuffer() {
        final VBO vbo = new VBO(GL_ELEMENT_ARRAY_BUFFER, GL_STATIC_DRAW);
        vbos.add(vbo);
        return vbo;
    }

    @NotNull
    @Override
    public VAO bind() {
        glBindVertexArray(id);
        return this;
    }

    @Override
    public void unbind() {
        glBindVertexArray(0);
    }

    @Override
    public void dispose() {
        for (VBO vbo : vbos) {
            vbo.dispose();
        }

        glBindVertexArray(0);
        glDeleteVertexArrays(id);
    }
}
