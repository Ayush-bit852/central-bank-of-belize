package bz.gov.centralbank.servlets;

import bz.gov.centralbank.dao.AccountDao;
import bz.gov.centralbank.dao.TransactionDao;
import bz.gov.centralbank.models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

public class DashboardServlet extends HttpServlet {

    private final AccountDao accountDao = new AccountDao();
    private final TransactionDao transactionDao = new TransactionDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = (User) req.getSession().getAttribute("user");
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        try {
            req.setAttribute("accounts", accountDao.findByUserId(user.getId()));
            req.setAttribute("transactions", transactionDao.findRecentByUserId(user.getId(), 20));
            req.getRequestDispatcher("/WEB-INF/jsp/dashboard.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Error loading dashboard", e);
        }
    }
}
