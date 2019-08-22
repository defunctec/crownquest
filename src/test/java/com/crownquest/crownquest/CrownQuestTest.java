package com.crownquest.crownquest;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.Assert.*;

public class CrownQuestTest {

    @Test
    public void testWallet() throws SQLException, IOException, ParseException, java.text.ParseException {
        if(CrownQuest.CROWN_NODE_USERNAME!=null&&System.getenv("POSTGRES_1_PORT_5432_TCP_ADDR")!=null&&System.getenv("POSTGRES_ENV_POSTGRES_USER")!=null&&System.getenv("POSTGRES_ENV_POSTGRES_PASSWORD")!=null) {
            final String db_url = "jdbc:postgresql://" + System.getenv("POSTGRES_1_PORT_5432_TCP_ADDR") + ":" + System.getenv("POSTGRES_1_PORT_5432_TCP_PORT") + "/CrownQuest";

            System.out.println("full node username: "+CrownQuest.CROWN_NODE_USERNAME);
            System.out.println("full node host: "+CrownQuest.CROWN_NODE_HOST);
            System.out.println("full node port: "+CrownQuest.CROWN_NODE_PORT);
            System.out.println("full node password: "+CrownQuest.CROWN_NODE_PASSWORD);
            System.out.println("db url: "+db_url);

            System.out.println("db username: "+System.getenv("POSTGRES_ENV_POSTGRES_USER"));
            System.out.println("db password: "+System.getenv("POSTGRES_ENV_POSTGRES_PASSWORD"));

            UUID test_uuid= UUID.fromString("123e4567-e89b-12d3-a456-42665544000");

            Connection db_con = DriverManager.getConnection(db_url, System.getenv("POSTGRES_ENV_POSTGRES_USER"), System.getenv("POSTGRES_ENV_POSTGRES_PASSWORD"));
            Statement st = db_con.createStatement();
            ResultSet rs = st.executeQuery("SELECT VERSION()");

            if (rs.next()) {
                System.out.println(rs.getString(1));
            }
            DBMigrationCheck migration = new DBMigrationCheck(db_con);
            User user=new User(db_con,test_uuid);
            System.out.println("Test user address: "+user.wallet.address);
            Wallet test_world_wallet=new Wallet("d6918becd01bfaa50a848eb346c38739fb8b9ce6d10124f8758a94498bd21bb8","03870b86fbdee06f9104ac4ece5435d4d9283366508cee764df84e7b5747605ec0","mysVa261EMDtnUYDLY54TEaVAoEbHudY8D","cUmo47gffcGAePgvLY97nwqd5y1KPTnAi5S7soVio5PYzFayBfhf");
            assertTrue(test_world_wallet.payment(user.wallet.address, Long.valueOf(10)));
            LegacyWallet legacy_wallet=new LegacyWallet(test_uuid.toString());
            test_world_wallet.payment(legacy_wallet.getAccountAddress(), Long.valueOf(10000));
            System.out.println("legacy wallet balance: "+legacy_wallet.getBalance(2));
            System.out.println("legacy wallet address: "+legacy_wallet.getAccountAddress());
            if(legacy_wallet.getBalance(2)>0) {
                legacy_wallet.sendFrom(user.wallet.address,legacy_wallet.getBalance(2));
            }
        }

    }
}
