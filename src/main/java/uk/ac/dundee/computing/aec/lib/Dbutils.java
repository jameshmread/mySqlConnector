package uk.ac.dundee.computing.aec.lib;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.sql.DataSource;

public class Dbutils {

    private static final void listContext(Context ctx, String indent) {
        try {
            NamingEnumeration list = ctx.listBindings("");
            while (list.hasMore()) {
                Binding item = (Binding) list.next();
                String className = item.getClassName();
                String name = item.getName();
                System.out.println("" + className + " " + name);
                Object o = item.getObject();
                if (o instanceof javax.naming.Context) {
                    listContext((Context) o, indent + " ");
                }
            }
        } catch (NamingException ex) {
            System.out.println("JNDI failure: " + ex);
        }
    }

    /**
     * Assembles a DataSource from JNDI.
     */
    public DataSource assemble(ServletConfig config) throws ServletException {
        DataSource _ds = null;
        String dataSourceName = config.getInitParameter("data-source");
        System.out.println("Data Source Parameter " + dataSourceName);
        if (dataSourceName == null) {
            throw new ServletException("data-source must be specified");
        }
        Context envContext = null;
        try {
            Context ic = new InitialContext();
            System.out.println("initial context " + ic.getNameInNamespace());
            envContext = (Context) ic.lookup("java:/comp/env");
            System.out.println("envcontext  " + envContext);
            listContext(envContext, "");
        } catch (Exception et) {
            throw new ServletException("Can't get contexts " + et);
        }
        // _ds = (DataSource) ic.lookup("java:"+dataSourceName);
        // _ds = (DataSource) ic.lookup("java:comp/env/" );
        try {
            _ds = (DataSource) envContext.lookup(dataSourceName);

            if (_ds == null) {
                throw new ServletException(dataSourceName
                        + " is an unknown data-source.");
            }
        } catch (NamingException e) {
            throw new ServletException("Cant find datasource name " + dataSourceName + " Error " + e);
        }
        this.CreateSchema(_ds);
        return _ds;

    }

    // create the schema if it doesn't exist
    private void CreateSchema(DataSource _ds) {
        PreparedStatement pmst = null;
        Connection Conn;
        try {
            Conn = _ds.getConnection();
        } catch (Exception et) {
            return;
        }

        String sqlQuery = "CREATE TABLE IF NOT EXISTS `comment` ("
                + "`idcomment` INT NOT NULL AUTO_INCREMENT," + "`comment` VARCHAR(45) NULL,"
                + "PRIMARY KEY (`idcomment`))" + "ENGINE = InnoDB;";
        try {
            pmst = Conn.prepareStatement(sqlQuery);
            pmst.executeUpdate();
        } catch (Exception ex) {
            System.out.println("Can not create table " + ex);
            return;
        }
        sqlQuery = "insert into comment (comment) values ('test');";
        try {
            pmst = Conn.prepareStatement(sqlQuery);
            pmst.executeUpdate();
        } catch (Exception ex) {
            System.out.println("Can not insert table " + ex);
            return;
        }

    }
}
