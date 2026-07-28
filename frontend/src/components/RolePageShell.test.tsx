import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { RolePageShell } from "./RolePageShell";
import { SidebarProvider } from "@/contexts/SidebarContext";

vi.mock("@/hooks/useNotificationStream", () => ({
  useNotificationStream: () => {},
}));

vi.mock("@/components/DashboardHeader", () => ({
  DashboardHeader: ({ title }: { title: string }) => <header>{title}</header>,
}));

vi.mock("@/hooks/useAuth", () => ({
  useLogout: () => ({ mutate: vi.fn(), isPending: false }),
}));

vi.mock("@/contexts/RoleContext", async (importOriginal) => ({
  ...(await importOriginal<object>()),
  useRole: () => ({ user: { userId: 1, email: "admin@clinic.test", role: "ADMIN" } }),
}));

function renderShell() {
  return render(
    <MemoryRouter>
      <SidebarProvider>
        <RolePageShell title="Dashboard">
          <p>page body</p>
        </RolePageShell>
      </SidebarProvider>
    </MemoryRouter>,
  );
}

describe("RolePageShell sidebar collapse", () => {
  beforeEach(() => localStorage.clear());

  it("keeps the content flush with the sidebar in both states", async () => {
    const user = userEvent.setup();
    const { container } = renderShell();

    const main = container.querySelector("main")!;
    const aside = container.querySelector("aside")!;

    expect(aside).toHaveClass("w-[260px]");
    expect(main).toHaveClass("ml-[260px]");

    await user.click(screen.getByLabelText("Collapse sidebar"));

    // Regression: main was pinned at ml-[260px], leaving a gap once the
    // sidebar shrank to 72px.
    expect(aside).toHaveClass("w-[72px]");
    expect(main).toHaveClass("ml-[72px]");
    expect(main).not.toHaveClass("ml-[260px]");

    await user.click(screen.getByLabelText("Expand sidebar"));

    expect(aside).toHaveClass("w-[260px]");
    expect(main).toHaveClass("ml-[260px]");
  });

  it("animates the margin so the content slides rather than jumps", () => {
    const { container } = renderShell();
    const main = container.querySelector("main")!;
    const aside = container.querySelector("aside")!;

    expect(main).toHaveClass("transition-[margin-left]", "duration-300");
    expect(aside).toHaveClass("transition-all", "duration-300");
  });

  it("remembers the collapsed state across a remount", async () => {
    const user = userEvent.setup();
    const first = renderShell();

    await user.click(screen.getByLabelText("Collapse sidebar"));
    first.unmount();

    // Each page mounts its own RolePageShell, so navigating used to reset this.
    const { container } = renderShell();
    expect(container.querySelector("main")).toHaveClass("ml-[72px]");
  });
});
