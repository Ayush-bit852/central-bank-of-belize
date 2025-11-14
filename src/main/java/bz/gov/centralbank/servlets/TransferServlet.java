package bz.gov.centralbank.servlets;

import bz.gov.centralbank.dao.AccountDao;
import bz.gov.centralbank.models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

public class TransferServlet extends HttpServlet {

    private final AccountDao accountDao = new AccountDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String fromIdStr = req.getParameter("fromAccountId");
        String toIdStr = req.getParameter("toAccountId");
        String amountStr = req.getParameter("amount");
        String description = req.getParameter("description");

        try {
            int fromId = Integer.parseInt(fromIdStr);
            int toId = Integer.parseInt(toIdStr);
            BigDecimal amount = new BigDecimal(amountStr);

            boolean success = accountDao.transfer(fromId, toId, amount, description);
            if (!success) {
                req.setAttribute("error", "Transfer failed. Check balances and try again.");
            } else {
                req.setAttribute("message", "Transfer completed successfully.");
            }
        } catch (NumberFormatException | SQLException e) {
            req.setAttribute("error", "Invalid transfer request.");
        }

        // Reload dashboard
        req.getRequestDispatcher("/dashboard").forward(req, resp);
    }
}
