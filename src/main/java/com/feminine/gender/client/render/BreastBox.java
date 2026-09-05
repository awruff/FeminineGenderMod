package com.feminine.gender.client.render;

import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;

public class BreastBox {

    private static final float[][] NORMALS = {
        {1F, 0F, 0F},
        {-1F, 0F, 0F},
        {0F, -1F, 0F},
        {0F, 1F, 0F},
        {0F, 0F, -1F},
    };

    private final Quad[] quads;

    public BreastBox(int texWidth, int texHeight, float x, float y, float z, int dx, int dy, int dz, float delta, UVQuad[] uvs) {
        float maxX = x + dx;
        float maxY = y + dy;
        float maxZ = z + dz;
        x -= delta;
        y -= delta;
        z -= delta;
        maxX += delta;
        maxY += delta;
        maxZ += delta;

        Vertex v0 = new Vertex(maxX, y, z);
        Vertex v1 = new Vertex(maxX, maxY, z);
        Vertex v2 = new Vertex(x, maxY, z);
        Vertex v3 = new Vertex(x, y, maxZ);
        Vertex v4 = new Vertex(maxX, y, maxZ);
        Vertex v5 = new Vertex(maxX, maxY, maxZ);
        Vertex v6 = new Vertex(x, maxY, maxZ);
        Vertex v7 = new Vertex(x, y, z);

        Vertex[][] faces = {
            {v4, v0, v1, v5},
            {v7, v3, v6, v2},
            {v4, v3, v7, v0},
            {v1, v2, v6, v5},
            {v0, v7, v2, v1},
        };

        this.quads = new Quad[faces.length];
        for (int i = 0; i < faces.length; i++) {
            UVQuad uv = i < uvs.length ? uvs[i] : null;

            if (uv == null || uv.isUnused()) {
                continue;
            }
            this.quads[i] = new Quad(faces[i], uv, texWidth, texHeight, NORMALS[i]);
        }
    }

    public void render(float scale) {
        BufferBuilder buffer = Tesselator.getInstance().getBuffer();
        for (Quad quad : quads) {
            if (quad == null) {
                continue;
            }
            buffer.begin(7, DefaultVertexFormat.ENTITY);
            for (int i = 0; i < 4; i++) {
                Vertex vertex = quad.vertices[i];
                buffer.vertex(vertex.x * scale, vertex.y * scale, vertex.z * scale)
                    .texture(quad.u[i], quad.v[i])
                    .normal(quad.normal[0], quad.normal[1], quad.normal[2])
                    .nextVertex();
            }
            Tesselator.getInstance().end();
        }
    }

    private static final class Vertex {

        final float x;
        final float y;
        final float z;

        Vertex(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static final class Quad {

        final Vertex[] vertices;
        final float[] u = new float[4];
        final float[] v = new float[4];
        final float[] normal;

        Quad(Vertex[] vertices, UVQuad uv, float texWidth, float texHeight, float[] normal) {
            this.vertices = vertices;
            this.normal = normal;

            float u1 = uv.x1 / texWidth;
            float u2 = uv.x2 / texWidth;
            float v1 = uv.y1 / texHeight;
            float v2 = uv.y2 / texHeight;

            u[0] = u2; v[0] = v1;
            u[1] = u1; v[1] = v1;
            u[2] = u1; v[2] = v2;
            u[3] = u2; v[3] = v2;
        }
    }
}
