import { render, screen, fireEvent } from "@testing-library/react";
import Login from "./Login";
import { describe, it, expect, vi } from "vitest";

const mockLogin = vi.fn();
vi.mock("@/hooks/useAuth", () => ({
  useLogin: () => ({
    mutate: mockLogin,
    isPending: false,
    error: null,
  }),
}));

describe("Login", () => {
  it("renders the login form", () => {
    render(<Login />);

    expect(screen.getByPlaceholderText(/email/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /sign in/i })).toBeInTheDocument();
  });

  it("renders demo credential buttons", () => {
    render(<Login />);

    expect(screen.getByText("Admin")).toBeInTheDocument();
    expect(screen.getByText("Doctor")).toBeInTheDocument();
    expect(screen.getByText("Patient")).toBeInTheDocument();
  });

  it("fills email and password when demo button is clicked", () => {
    render(<Login />);

    fireEvent.click(screen.getByText("Admin"));

    const emailInput = screen.getByPlaceholderText(/email/i) as HTMLInputElement;
    const passwordInput = screen.getByPlaceholderText(/password/i) as HTMLInputElement;
    expect(emailInput.value).toBe("admin@clinic.com");
    expect(passwordInput.value).toBe("admin123");
  });

  it("calls login mutation on form submit", () => {
    render(<Login />);

    fireEvent.change(screen.getByPlaceholderText(/email/i), {
      target: { value: "test@test.com" },
    });
    fireEvent.change(screen.getByPlaceholderText(/password/i), {
      target: { value: "password123" },
    });
    fireEvent.click(screen.getByRole("button", { name: /sign in/i }));

    expect(mockLogin).toHaveBeenCalledWith({
      email: "test@test.com",
      password: "password123",
    });
  });

  it("toggles password visibility", () => {
    render(<Login />);

    const passwordInput = screen.getByPlaceholderText(/password/i) as HTMLInputElement;
    expect(passwordInput.type).toBe("password");

    const toggleButton = passwordInput.parentElement?.querySelector("button");
    if (toggleButton) {
      fireEvent.click(toggleButton);
      expect(passwordInput.type).toBe("text");
    }
  });
});
