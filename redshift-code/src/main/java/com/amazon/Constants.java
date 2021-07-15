package com.amazon;

public class Constants {
    public static final String PK_TEMPLATE = "SELECT    f.attnum AS number,    c.relname AS table_name,    f.attname AS column_name FROM pg_attribute f    JOIN pg_class c ON c.oid = f.attrelid    JOIN pg_type t ON t.oid = f.atttypid    LEFT JOIN pg_attrdef d ON d.adrelid = c.oid AND d.adnum = f.attnum    LEFT JOIN pg_namespace n ON n.oid = c.relnamespace    LEFT JOIN pg_constraint p ON p.conrelid = c.oid AND f.attnum = ANY (p.conkey)    LEFT JOIN pg_class AS g ON p.confrelid = g.oid WHERE c.relkind = 'r'::char    AND n.nspname = '%s' AND c.relname = '%s'     AND p.contype = 'p'    AND f.attnum > 0";
    public static final String DK_SK_TEMPLATE = "select \"column\", type, distkey, sortkey  from pg_table_def where schemaname = '%s' and tablename = '%s' and (sortkey <> 0 or distkey = true)";
    public static final String DATETIME_TEMPLATE = "select \"column\" from pg_table_def where schemaname = '%s' and tablename = '%s' and \"column\" in (%s)";
    public static final String SIZE_TEMPLATE = "SELECT \"schema\", \"table\", diststyle, sortkey1, sortkey1_enc, sortkey_num, \"size\", \"size\"/1024 as size_in_GB, tbl_rows, skew_sortkey1, skew_rows, vacuum_sort_benefit FROM SVV_TABLE_INFO order by size desc";
}
