package com.adiwave.faultycode;

import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/*
 * @FIXME: CODE-REVIEW-STYLE SHOULD BLOCK THIS CODE BEFORE IT GET MERGED TO MASTER!
 * @FIXME: ANALYZE THIS CODE AND FIX THE MANY ISSUES IT CONTAINS!
 * THIS IS A FAULTY CODE EXAMPLE
 * DO NOT USE IN PRODUCTION
 * IT COULD LEAD TO SECURITY ISSUES,
 * SQL INJECTION VULNERABILITIES AND CONCURRENCY ISSUES
 **/

@RestController
public class PaymentController {

    private Connection connection; // reused globally

    public PaymentController() throws Exception {
        // Hardcoded credentials (security issue)
        connection = DriverManager.getConnection(
                "jdbc:h2:mem:payments;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                "sa",
                ""
        );
    }

    @PostMapping("/payments")
    public Map<String, Object> pay(@RequestBody Map<String, String> body) throws Exception {
        String sender = body.get("senderId");
        String receiver = body.get("receiverId");
        String amount = body.get("amount");

        // No validation, SQL injection vulnerability
        Statement st = connection.createStatement();
        ResultSet senderBalance = st.executeQuery(
                "SELECT balance FROM users WHERE id = " + sender
        );

        senderBalance.next();
        double bal = senderBalance.getDouble("balance");

        double amt = Double.parseDouble(amount);

        // No concurrency control → race condition
        if (bal > amt) {
            st.execute("UPDATE users SET balance = balance - " + amt + " WHERE id = " + sender);
            st.execute("UPDATE users SET balance = balance + " + amt + " WHERE id = " + receiver);
            st.execute("INSERT INTO payments (sender,receiver,amount,status) VALUES ('"
                    + sender + "','" + receiver + "'," + amt + ",'OK')");
        }

        Map<String, Object> res = new HashMap<>();
        res.put("status", "OK"); // wrong result even if failure
        return res;
    }

    @GetMapping("/payments/{id}")
    public Map<String, Object> getPay(@PathVariable String id) throws Exception {
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM payments WHERE id = " + id);
        rs.next();
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("sender", rs.getString("sender"));
        m.put("receiver", rs.getString("receiver"));
        m.put("amount", rs.getDouble("amount"));
        m.put("status", rs.getString("status"));
        return m;
    }
}
