/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.db;

import nxt.util.Convert;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public interface DbKey {

    abstract class Factory<T> {

        private final String pkClause;
        private final String pkColumns;
        private final String selfJoinClause;

        protected Factory(String pkClause, String pkColumns, String selfJoinClause) {
            this.pkClause = pkClause;
            this.pkColumns = pkColumns;
            this.selfJoinClause = selfJoinClause;
        }

        public abstract DbKey newKey(T t);

        public abstract DbKey newKey(ResultSet rs) throws SQLException;

        public T newEntity(DbKey dbKey) {
            throw new UnsupportedOperationException("Not implemented");
        }

        public final String getPKClause() {
            return pkClause;
        }

        public final String getPKColumns() {
            return pkColumns;
        }

        // expects tables to be named a and b
        public final String getSelfJoinClause() {
            return selfJoinClause;
        }

    }

    abstract class SmartFactory<T> extends Factory<T> {
        SmartFactory(String... columns) {
            super(pkClause(columns), pkColumns(columns), selfJoinClause(columns));
        }

        // visible for testing
        static String selfJoinClause(String... columns) {
            return Stream.of(columns).map(s -> "a." + s + " = b." + s).collect(joining(" AND "));
        }

        // visible for testing
        static String pkColumns(String... columns) {
            return Stream.of(columns).collect(joining(", "));
        }

        // visible for testing
        static String pkClause(String... columns) {
            return " WHERE " + Stream.of(columns).map(s -> s + " = ? ").collect(joining("AND "));
        }
    }


    default int setPK(PreparedStatement pstmt) throws SQLException {
        return setPK(pstmt, 1);
    }

    int setPK(PreparedStatement pstmt, int index) throws SQLException;


    abstract class LongKeyFactory<T> extends Factory<T> {

        private final String idColumn;

        public LongKeyFactory(String idColumn) {
            super(" WHERE " + idColumn + " = ? ",
                    idColumn,
                    " a." + idColumn + " = b." + idColumn + " ");
            this.idColumn = idColumn;
        }

        @Override
        public DbKey newKey(ResultSet rs) throws SQLException {
            return new LongKey(rs.getLong(idColumn));
        }

        public DbKey newKey(long id) {
            return new LongKey(id);
        }

    }

    abstract class StringKeyFactory<T> extends Factory<T> {

        private final String idColumn;

        public StringKeyFactory(String idColumn) {
            super(" WHERE " + idColumn + " = ? ",
                    idColumn,
                    " a." + idColumn + " = b." + idColumn + " ");
            this.idColumn = idColumn;
        }

        @Override
        public DbKey newKey(ResultSet rs) throws SQLException {
            return new StringKey(rs.getString(idColumn));
        }

        public DbKey newKey(String id) {
            return new StringKey(id);
        }

    }

    abstract class HashKeyFactory<T> extends Factory<T> {

        // keep long id (non-unique) for index performance
        private final String idColumn;
        private final String hashColumn;

        public HashKeyFactory(String hashColumn, String idColumn) {
            super(" WHERE " + idColumn + " = ? AND " + hashColumn + " = ?",
                    idColumn + ", " + hashColumn,
                    " a." + idColumn + " = b." + idColumn + " AND a." + hashColumn + " = b." + hashColumn + " ");
            this.idColumn = idColumn;
            this.hashColumn = hashColumn;
        }

        @Override
        public DbKey newKey(ResultSet rs) throws SQLException {
            return new HashKey(rs.getBytes(hashColumn), rs.getLong(idColumn));
        }

        public DbKey newKey(byte[] hash) {
            return new HashKey(hash);
        }

        public DbKey newKey(byte[] hash, long id) {
            return new HashKey(hash, id);
        }
    }

    abstract class LongLongKeyFactory<T> extends Factory<T> {

        private final String idColumnA;
        private final String idColumnB;

        public LongLongKeyFactory(String idColumnA, String idColumnB) {
            super(" WHERE " + idColumnA + " = ? AND " + idColumnB + " = ? ",
                    idColumnA + ", " + idColumnB,
                    " a." + idColumnA + " = b." + idColumnA + " AND a." + idColumnB + " = b." + idColumnB + " ");
            this.idColumnA = idColumnA;
            this.idColumnB = idColumnB;
        }

        @Override
        public DbKey newKey(ResultSet rs) throws SQLException {
            return new LongLongKey(rs.getLong(idColumnA), rs.getLong(idColumnB));
        }

        public DbKey newKey(long idA, long idB) {
            return new LongLongKey(idA, idB);
        }

    }

    abstract class HashLongKeyFactory<T> extends Factory<T> {

        private final String idColumnA;
        private final String hashColumnA;
        private final String idColumnB;

        public HashLongKeyFactory(String hashColumnA, String idColumnA, String idColumnB) {
            super(" WHERE " + idColumnA + " = ? AND " + hashColumnA + " = ? AND " + idColumnB + " = ? ",
                    idColumnA + ", " + hashColumnA + ", " + idColumnB,
                    " a." + idColumnA + " = b." + idColumnA + " AND a." + hashColumnA + " = b." + hashColumnA
                            + " AND a." + idColumnB + " = b." + idColumnB + " ");
            this.idColumnA = idColumnA;
            this.hashColumnA = hashColumnA;
            this.idColumnB = idColumnB;
        }

        @Override
        public DbKey newKey(ResultSet rs) throws SQLException {
            return new HashLongKey(rs.getBytes(hashColumnA), rs.getLong(idColumnA), rs.getLong(idColumnB));
        }

        public DbKey newKey(byte[] hashA, long idB) {
            return new HashLongKey(hashA, idB);
        }

        public DbKey newKey(byte[] hashA, long idA, long idB) {
            return new HashLongKey(hashA, idA, idB);
        }

    }

    abstract class HashHashKeyFactory<T> extends Factory<T> {

        private final String idColumnA;
        private final String hashColumnA;
        private final String idColumnB;
        private final String hashColumnB;

        public HashHashKeyFactory(String hashColumnA, String idColumnA, String hashColumnB, String idColumnB) {
            super(" WHERE " + idColumnA + " = ? AND " + hashColumnA + " = ? AND " + idColumnB + " = ? AND "
                            + hashColumnB + " = ? ",
                    idColumnA + ", " + hashColumnA + ", " + idColumnB + ", " + hashColumnB,
                    " a." + idColumnA + " = b." + idColumnA + " AND a." + hashColumnA + " = b." + hashColumnA
                            + " AND a." + idColumnB + " = b." + idColumnB + " " + " AND a." + hashColumnB + " = b." + hashColumnB);
            this.idColumnA = idColumnA;
            this.hashColumnA = hashColumnA;
            this.idColumnB = idColumnB;
            this.hashColumnB = hashColumnB;
        }

        @Override
        public DbKey newKey(ResultSet rs) throws SQLException {
            return new HashHashKey(rs.getBytes(hashColumnA), rs.getLong(idColumnA),
                    rs.getBytes(hashColumnB), rs.getLong(idColumnB));
        }

        public DbKey newKey(byte[] hashA, byte[] hashB) {
            return new HashHashKey(hashA, hashB);
        }

        public DbKey newKey(byte[] hashA, long idA, byte[] hashB, long idB) {
            return new HashHashKey(hashA, idA, hashB, idB);
        }

    }

    abstract class HashHashLongKeyFactory<T> extends Factory<T> {

        private final String idColumnA;
        private final String hashColumnA;
        private final String idColumnB;
        private final String hashColumnB;
        private final String idColumnC;

        public HashHashLongKeyFactory(String hashColumnA, String idColumnA, String hashColumnB, String idColumnB, String idColumnC) {
            super(" WHERE " + idColumnA + " = ? AND " + hashColumnA + " = ? AND " + idColumnB + " = ? AND "
                            + hashColumnB + " = ? AND " + idColumnC + " = ? ",
                    idColumnA + ", " + hashColumnA + ", " + idColumnB + ", " + hashColumnB + ", " + idColumnC,
                    " a." + idColumnA + " = b." + idColumnA + " AND a." + hashColumnA + " = b." + hashColumnA
                            + " AND a." + idColumnB + " = b." + idColumnB + " " + " AND a." + hashColumnB + " = b." + hashColumnB
                            + " AND a." + idColumnC + " = b." + idColumnC);
            this.idColumnA = idColumnA;
            this.hashColumnA = hashColumnA;
            this.idColumnB = idColumnB;
            this.hashColumnB = hashColumnB;
            this.idColumnC = idColumnC;
        }

        @Override
        public DbKey newKey(ResultSet rs) throws SQLException {
            return new HashHashLongKey(rs.getBytes(hashColumnA), rs.getLong(idColumnA),
                    rs.getBytes(hashColumnB), rs.getLong(idColumnB), rs.getLong(idColumnC));
        }

        public DbKey newKey(byte[] hashA, byte[] hashB, long idC) {
            return new HashHashLongKey(hashA, hashB, idC);
        }

        public DbKey newKey(byte[] hashA, long idA, byte[] hashB, long idB, long idC) {
            return new HashHashLongKey(hashA, idA, hashB, idB, idC);
        }

    }

    abstract class LongStringKeyFactory<T> extends SmartFactory<T> {
        private final String idColumnA;
        private final String idColumnB;

        protected LongStringKeyFactory(String idColumnA, String idColumnB) {
            super(idColumnA, idColumnB);
            this.idColumnA = idColumnA;
            this.idColumnB = idColumnB;
        }

        @Override
        public DbKey newKey(ResultSet rs) throws SQLException {
            return new LongStringKey(rs.getLong(idColumnA), rs.getString(idColumnB));
        }

        public DbKey newKey(long a, String b) {
            return new LongStringKey(a, b);
        }
    }

    final class LongKey implements DbKey {

        private final long id;

        private LongKey(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }

        @Override
        public int setPK(PreparedStatement pstmt, int index) throws SQLException {
            pstmt.setLong(index, id);
            return index + 1;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof LongKey && ((LongKey) o).id == id;
        }

        @Override
        public int hashCode() {
            return (int) (id ^ (id >>> 32));
        }

    }

    final class StringKey implements DbKey {

        private final String id;

        private StringKey(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        @Override
        public int setPK(PreparedStatement pstmt, int index) throws SQLException {
            pstmt.setString(index, id);
            return index + 1;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof StringKey && (id != null ? id.equals(((StringKey) o).id) : ((StringKey) o).id == null);
        }

        @Override
        public int hashCode() {
            return id != null ? id.hashCode() : 0;
        }

    }

    final class HashKey implements DbKey {

        private final long id;
        private final byte[] hash;

        private HashKey(byte[] hash) {
            this.hash = hash;
            this.id = Convert.fullHashToId(hash);
        }

        private HashKey(byte[] hash, long id) {
            this.hash = hash;
            this.id = id;
        }

        public byte[] getHash() {
            return hash;
        }

        public long getId() {
            return id;
        }

        @Override
        public int setPK(PreparedStatement pstmt, int index) throws SQLException {
            pstmt.setLong(index, id);
            pstmt.setBytes(index + 1, hash);
            return index + 2;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof HashKey && Arrays.equals(hash, ((HashKey) o).hash);
        }

        @Override
        public int hashCode() {
            return Long.hashCode(id);
        }

    }

    final class LongLongKey implements DbKey {

        private final long idA;
        private final long idB;

        private LongLongKey(long idA, long idB) {
            this.idA = idA;
            this.idB = idB;
        }

        public long[] getId() {
            return new long[]{idA, idB};
        }

        @Override
        public int setPK(PreparedStatement pstmt, int index) throws SQLException {
            pstmt.setLong(index, idA);
            pstmt.setLong(index + 1, idB);
            return index + 2;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof LongLongKey && ((LongLongKey) o).idA == idA && ((LongLongKey) o).idB == idB;
        }

        @Override
        public int hashCode() {
            return (int) (idA ^ (idA >>> 32)) ^ (int) (idB ^ (idB >>> 32));
        }

    }

    final class HashLongKey implements DbKey {

        private final long idA;
        private final byte[] hashA;
        private final long idB;

        private HashLongKey(byte[] hashA, long idB) {
            this.hashA = hashA;
            this.idA = Convert.fullHashToId(hashA);
            this.idB = idB;
        }

        private HashLongKey(byte[] hashA, long idA, long idB) {
            this.idA = idA;
            this.idB = idB;
            this.hashA = hashA;
        }

        public byte[] getHashA() {
            return hashA;
        }

        public long getIdB() {
            return idB;
        }

        @Override
        public int setPK(PreparedStatement pstmt, int index) throws SQLException {
            pstmt.setLong(index, idA);
            pstmt.setBytes(index + 1, hashA);
            pstmt.setLong(index + 2, idB);
            return index + 3;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof HashLongKey && ((HashLongKey) o).idB == idB
                    && Arrays.equals(hashA, ((HashLongKey) o).hashA);
        }

        @Override
        public int hashCode() {
            return (int) (idA ^ (idA >>> 32)) ^ (int) (idB ^ (idB >>> 32));
        }

    }

    final class HashHashKey implements DbKey {

        private final long idA;
        private final byte[] hashA;
        private final long idB;
        private final byte[] hashB;

        private HashHashKey(byte[] hashA, byte[] hashB) {
            this.hashA = hashA;
            this.idA = Convert.fullHashToId(hashA);
            this.hashB = hashB;
            this.idB = Convert.fullHashToId(hashB);
        }

        private HashHashKey(byte[] hashA, long idA, byte[] hashB, long idB) {
            this.idA = idA;
            this.idB = idB;
            this.hashA = hashA;
            this.hashB = hashB;
        }

        public byte[] getHashA() {
            return hashA;
        }

        public byte[] getHashB() {
            return hashB;
        }

        @Override
        public int setPK(PreparedStatement pstmt, int index) throws SQLException {
            pstmt.setLong(index, idA);
            pstmt.setBytes(index + 1, hashA);
            pstmt.setLong(index + 2, idB);
            pstmt.setBytes(index + 3, hashB);
            return index + 4;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof HashHashKey && Arrays.equals(hashB, ((HashHashKey) o).hashB)
                    && Arrays.equals(hashA, ((HashHashKey) o).hashA);
        }

        @Override
        public int hashCode() {
            return (int) (idA ^ (idA >>> 32)) ^ (int) (idB ^ (idB >>> 32));
        }

    }

    final class HashHashLongKey implements DbKey {

        private final long idA;
        private final byte[] hashA;
        private final long idB;
        private final byte[] hashB;
        private final long idC;

        private HashHashLongKey(byte[] hashA, byte[] hashB, long idC) {
            this.hashA = hashA;
            this.idA = Convert.fullHashToId(hashA);
            this.hashB = hashB;
            this.idB = Convert.fullHashToId(hashB);
            this.idC = idC;
        }

        private HashHashLongKey(byte[] hashA, long idA, byte[] hashB, long idB, long idC) {
            this.idA = idA;
            this.idB = idB;
            this.hashA = hashA;
            this.hashB = hashB;
            this.idC = idC;
        }

        public byte[] getHashA() {
            return hashA;
        }

        public byte[] getHashB() {
            return hashB;
        }

        public long getIdC() {
            return idC;
        }

        @Override
        public int setPK(PreparedStatement pstmt, int index) throws SQLException {
            pstmt.setLong(index, idA);
            pstmt.setBytes(index + 1, hashA);
            pstmt.setLong(index + 2, idB);
            pstmt.setBytes(index + 3, hashB);
            pstmt.setLong(index + 4, idC);
            return index + 5;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof HashHashLongKey && idC == ((HashHashLongKey) o).idC && Arrays.equals(hashB, ((HashHashLongKey) o).hashB)
                    && Arrays.equals(hashA, ((HashHashLongKey) o).hashA);
        }

        @Override
        public int hashCode() {
            return (int) (idA ^ (idA >>> 32)) ^ (int) (idB ^ (idB >>> 32)) ^ Long.hashCode(idC);
        }

    }

    final class LongStringKey implements DbKey {
        private final long idA;
        private final String idB;

        private LongStringKey(long idA, String idB) {
            this.idA = idA;
            this.idB = idB;
        }

        @Override
        public int setPK(PreparedStatement pstmt, int index) throws SQLException {
            pstmt.setLong(index, idA);
            pstmt.setString(index + 1, idB);
            return index + 2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LongStringKey)) return false;
            LongStringKey that = (LongStringKey) o;
            return idA == that.idA &&
                    Objects.equals(idB, that.idB);
        }

        @Override
        public int hashCode() {
            return Objects.hash(idA, idB);
        }
    }
}
