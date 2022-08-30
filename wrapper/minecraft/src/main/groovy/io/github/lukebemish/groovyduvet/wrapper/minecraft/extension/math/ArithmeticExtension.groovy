/*
 * Copyright (C) 2022 Luke Bemish
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, see <https://www.gnu.org/licenses/>.
 */

package io.github.lukebemish.groovyduvet.wrapper.minecraft.extension.math

import com.mojang.math.*
import groovy.transform.CompileStatic
import net.minecraft.core.BlockPos
import net.minecraft.core.Position
import net.minecraft.core.Vec3i
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.BooleanOp
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import org.codehaus.groovy.runtime.DefaultGroovyMethods

@CompileStatic
class ArithmeticExtension {
    // BlockPos
    static BlockPos plus(BlockPos self, Vec3i other) {
        return self.offset(other)
    }

    static BlockPos negative(BlockPos self) {
        return self * -1
    }

    static BlockPos div(BlockPos self, int scalar) {
        return new BlockPos(self.x.intdiv(scalar) as int, self.y.intdiv(scalar) as int, self.z.intdiv(scalar) as int)
    }

    static BlockPos minus(BlockPos self, Vec3i other) {
        return self.subtract(other)
    }

    static BlockPos multiply(int self, BlockPos pos) {
        return pos * self
    }

    static <T> T asType(BlockPos self, Class<T> type) {
        return switch (type) {
            case Vec3, Position ->
                (T) new Vec3(self.x, self.y, self.z)
            case Vector3d ->
                (T) new Vector3d(self.x, self.y, self.z)
            case Vector3f ->
                (T) new Vector3f(self.x, self.y, self.z)
            case Quaternion ->
                (T) Quaternion.fromXYZ(self.x, self.y, self.z)
            case Vector4f ->
                (T) new Vector4f(self.x, self.y, self.z, 1f)
            default ->
                (T) DefaultGroovyMethods.asType(self, type)
        }
    }

    // Vec3i
    static Vec3i plus(Vec3i self, Vec3i other) {
        return self.offset(other)
    }

    static Vec3i negative(Vec3i self) {
        return self * -1
    }

    static Vec3i div(Vec3i self, int scalar) {
        return new Vec3i(self.x.intdiv(scalar) as int, self.y.intdiv(scalar) as int, self.z.intdiv(scalar) as int)
    }

    static Vec3i minus(Vec3i self, Vec3i other) {
        return self.subtract(other)
    }

    static Vec3i multiply(int self, Vec3i pos) {
        return pos * self
    }

    static <T> T asType(Vec3i self, Class<T> type) {
        return switch (type) {
            case Vec3, Position ->
                (T) new Vec3(self.x, self.y, self.z)
            case Vector3d ->
                (T) new Vector3d(self.x, self.y, self.z)
            case Vector3f ->
                (T) new Vector3f(self.x, self.y, self.z)
            case Vector4f ->
                (T) new Vector4f(self.x, self.y, self.z, 1f)
            case Quaternion ->
                (T) Quaternion.fromXYZ(self.x, self.y, self.z)
            case BlockPos ->
                (T) new BlockPos(self.x, self.y, self.z)
            default ->
                (T) DefaultGroovyMethods.asType(self, type)
        }
    }

    // Vec3/Position
    static Vec3 plus(Position self, Position other) {
        return new Vec3(self.x()+other.x(), self.y()+other.y(), self.z()+other.z())
    }

    static Vec3 multiply(Position self, double other) {
        return new Vec3(self.x()*other, self.y()*other, self.z()*other)
    }

    static Vec3 negative(Position self) {
        return multiply(self,-1)
    }

    static Vec3 div(Position self, double other) {
        return new Vec3(self.x()/other as double, self.y()/other as double, self.z()/other as double)
    }

    static Vec3 minus(Position self, Position other) {
        return new Vec3(self.x()-other.x(), self.y()-other.y(), self.z()-other.z())
    }

    static Vec3 multiply(double self, Position pos) {
        return multiply(pos,self)
    }

    static <T> T asType(Position self, Class<T> type) {
        return switch (type) {
            case Vec3i ->
                (T) new Vec3i(self.x(), self.y(), self.z())
            case Vector3d ->
                (T) new Vector3d(self.x(), self.y(), self.z())
            case Vector3f ->
                (T) new Vector3f(self.x() as float, self.y() as float, self.z() as float)
            case Vector4f ->
                (T) new Vector4f(self.x() as float, self.y() as float, self.z() as float, 1f)
            case Quaternion ->
                (T) Quaternion.fromXYZ(self.x() as float, self.y() as float, self.z() as float)
            case BlockPos ->
                (T) new BlockPos(self.x(), self.y(), self.z())
            default ->
                (T) DefaultGroovyMethods.asType(self, type)
        }
    }

    // Vector3d
    static Vector3d plus(Vector3d self, Vector3d other) {
        return new Vector3d(self.x+other.x, self.y+other.y, self.z+other.z)
    }

    static Vector3d multiply(Vector3d self, double other) {
        return new Vector3d(self.x*other, self.y*other, self.z*other)
    }

    static Vector3d negative(Vector3d self) {
        return multiply(self,-1)
    }

    static Vector3d div(Vector3d self, double other) {
        return new Vector3d(self.x/other as double, self.y/other as double, self.z/other as double)
    }

    static Vector3d minus(Vector3d self, Vector3d other) {
        return new Vector3d(self.x-other.x, self.y-other.y, self.z-other.z)
    }

    static Vector3d multiply(double self, Vector3d pos) {
        return multiply(pos,self)
    }

    static <T> T asType(Vector3d self, Class<T> type) {
        return switch (type) {
            case Vec3i ->
                (T) new Vec3i(self.x, self.y, self.z)
            case Vec3, Position ->
                (T) new Vec3(self.x, self.y, self.z)
            case Vector3f ->
                (T) new Vector3f(self.x as float, self.y as float, self.z as float)
            case Vector4f ->
                (T) new Vector4f(self.x as float, self.y as float, self.z as float, 1f)
            case Quaternion ->
                (T) Quaternion.fromXYZ(self.x as float, self.y as float, self.z as float)
            case BlockPos ->
                (T) new BlockPos(self.x, self.y, self.z)
            default ->
                (T) DefaultGroovyMethods.asType(self, type)
        }
    }

    // Vector3f
    static Vector3f plus(Vector3f self, Vector3f other) {
        return new Vector3f(self.x()+other.x() as float, self.y()+other.y() as float, self.z()+other.z() as float)
    }

    static Vector3f multiply(Vector3f self, double other) {
        return new Vector3f(self.x()*other as float, self.y()*other as float, self.z()*other as float)
    }

    static Vector3f negative(Vector3f self) {
        return multiply(self,-1)
    }

    static Vector3f div(Vector3f self, double other) {
        return new Vector3f(self.x()/other as float, self.y()/other as float, self.z()/other as float)
    }

    static Vector3f minus(Vector3f self, Vector3f other) {
        return new Vector3f(self.x()-other.x() as float, self.y()-other.y() as float, self.z()-other.z() as float)
    }

    static Vector3f multiply(double self, Vector3f pos) {
        return multiply(pos,self)
    }

    static <T> T asType(Vector3f self, Class<T> type) {
        return switch (type) {
            case Vec3i ->
                (T) new Vec3i(self.x(), self.y(), self.z())
            case Vec3, Position ->
                (T) new Vec3(self.x(), self.y(), self.z())
            case Vector3d ->
                (T) new Vector3d(self.x(), self.y(), self.z())
            case Vector4f ->
                (T) new Vector4f(self.x() as float, self.y() as float, self.z() as float, 1f)
            case Quaternion ->
                (T) Quaternion.fromXYZ(self.x(), self.y(), self.z())
            case BlockPos ->
                (T) new BlockPos(self.x(), self.y(), self.z())
            default ->
                (T) DefaultGroovyMethods.asType(self, type)
        }
    }

    // Vector4f
    static Vector4f plus(Vector4f self, Vector4f other) {
        return new Vector4f(self.x()+other.x() as float, self.y()+other.y() as float, self.z()+other.z() as float, self.w()+other.w() as float)
    }

    static Vector4f multiply(Vector4f self, double other) {
        return new Vector4f(self.x()*other as float, self.y()*other as float, self.z()*other as float, self.w()*other as float)
    }

    static Vector4f negative(Vector4f self) {
        return multiply(self,-1)
    }

    static Vector4f div(Vector4f self, double other) {
        return new Vector4f(self.x()/other as float, self.y()/other as float, self.z()/other as float, self.w()/other as float)
    }

    static Vector4f minus(Vector4f self, Vector4f other) {
        return new Vector4f(self.x()-other.x() as float, self.y()-other.y() as float, self.z()-other.z() as float, self.w()-other.w() as float)
    }

    static Vector4f multiply(double self, Vector4f pos) {
        return multiply(pos,self)
    }

    static <T> T asType(Vector4f self, Class<T> type) {
        return switch (type) {
            case Vec3i ->
                (T) new Vec3i(self.x(), self.y(), self.z())
            case Vec3, Position ->
                (T) new Vec3(self.x(), self.y(), self.z())
            case Vector3d ->
                (T) new Vector3d(self.x(), self.y(), self.z())
            case Vector3f ->
                (T) new Vector3f(self.x() as float, self.y() as float, self.z() as float)
            case BlockPos ->
                (T) new BlockPos(self.x(), self.y(), self.z())
            default ->
                (T) DefaultGroovyMethods.asType(self, type)
        }
    }

    // Quaternion

    static <T> T asType(Quaternion self, Class<T> type) {
        return switch (type) {
            case Vec3, Position, Vector3d, Vector3f, Vec3i, BlockPos ->
                (T) self.toXYZ().asType(type)
            case Matrix3f ->
                (T) new Matrix3f(self)
            case Matrix4f ->
                (T) new Matrix4f(self)
            default ->
                (T) DefaultGroovyMethods.asType(self, type)
        }
    }

    // Vec2
    static Vec2 plus(Vec2 self, Vec2 other) {
        return new Vec2(self.x+other.x as float, self.y+other.y as float)
    }

    static Vec2 multiply(Vec2 self, double other) {
        return new Vec2(self.x*other as float, self.y*other as float)
    }

    static Vec2 negative(Vec2 self) {
        return self.negated()
    }

    static Vec2 div(Vec2 self, double other) {
        return new Vec2(self.x/other as float, self.y/other as float)
    }

    static Vec2 minus(Vec2 self, Vec2 other) {
        return new Vec2(self.x-other.x as float, self.y-other.y as float)
    }

    static Vec2 multiply(double self, Vec2 pos) {
        return multiply(pos,self)
    }

    static <T> T asType(Vec2 self, Class<T> type) {
        return switch (type) {
            case Vec3i ->
                (T) new Vec3i(self.x, self.y, 0)
            case Vector3d ->
                (T) new Vector3d(self.x, self.y, 0)
            case Vec3, Position ->
                (T) new Vec3(self.x, self.y, 0)
            case Vector3f ->
                (T) new Vector3f(self.x, self.y, 0)
            case Vector4f ->
                (T) new Vector4f(self.x, self.y, 0, 0)
            case Quaternion ->
                (T) Quaternion.fromXYZ(self.x, self.y, 0)
            case BlockPos ->
                (T) new BlockPos(self.x, self.y, 0)
            default ->
                (T) DefaultGroovyMethods.asType(self, type)
        }
    }

    // AABB
    static AABB plus(AABB self, BlockPos other) {
        return self.move(other)
    }

    static AABB plus(AABB self, Vec3 other) {
        return self.move(other)
    }

    // VoxelShapes
    static VoxelShape plus(VoxelShape self, VoxelShape other) {
        return Shapes.join(self, other, BooleanOp.OR)
    }

    static VoxelShape or(VoxelShape self, VoxelShape other) {
        return Shapes.join(self, other, BooleanOp.OR)
    }

    static VoxelShape and(VoxelShape self, VoxelShape other) {
        return Shapes.join(self, other, BooleanOp.AND)
    }

    static VoxelShape xor(VoxelShape self, VoxelShape other) {
        return Shapes.join(self, other, BooleanOp.NOT_SAME)
    }

    static VoxelShape minus(VoxelShape self, VoxelShape other) {
        return Shapes.join(self, other, BooleanOp.NOT_SECOND)
    }

    // Matrix3f

    // Matrix4f
    static <T> T asType(Matrix4f self, Class<T> type) {
        return switch (type) {
            case Transformation ->
                (T) new Transformation(self)
            default ->
                (T) DefaultGroovyMethods.asType(self, type)
        }
    }

    // SymmetricGroup3
    static int getAt(SymmetricGroup3 self, int p) {
        return self.permutation(p)
    }

    static SymmetricGroup3 multiply(SymmetricGroup3 self, SymmetricGroup3 other) {
        return self.compose(other)
    }
    
    static SymmetricGroup3 negative(SymmetricGroup3 self) {
        return switch(self) {
            case SymmetricGroup3.P231 ->
                SymmetricGroup3.P312
            case SymmetricGroup3.P312 ->
                SymmetricGroup3.P231
            default ->
                self
        }
    }

    static <T> T asType(SymmetricGroup3 self, Class<T> type) {
        return switch (type) {
            case Matrix3f ->
                (T) self.transformation()
            default ->
                (T) DefaultGroovyMethods.asType(self, type)
        }
    }

    // OctahedralGroup
    static OctahedralGroup multiply(OctahedralGroup self, OctahedralGroup other) {
        return self.compose(other)
    }

    static OctahedralGroup negative(OctahedralGroup self) {
        return self.inverse()
    }

    static <T> T asType(OctahedralGroup self, Class<T> type) {
        return switch (type) {
            case Matrix3f ->
                (T) self.transformation()
            default ->
                (T) DefaultGroovyMethods.asType(self, type)
        }
    }

    // Transformation
    static Transformation multiply(Transformation self, Transformation other) {
        return self.compose(other)
    }

    static Transformation negative(Transformation self) {
        return self.inverse()
    }

    static <T> T asType(Transformation self, Class<T> type) {
        return switch (type) {
            case Matrix4f ->
                (T) self.matrix
            default ->
                (T) DefaultGroovyMethods.asType(self, type)
        }
    }
}
