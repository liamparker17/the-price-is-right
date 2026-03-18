---
name: frontend-design-ux
description: Create distinctive, production-grade frontend interfaces with exceptional design quality AND sound UX logic. Use this skill when the user asks to build web components, pages, artifacts, dashboards, forms, onboarding flows, apps, or any interactive UI — especially when usability, user flows, accessibility, feedback states, error handling, or interaction design matter. Triggers include: websites, landing pages, React components, HTML/CSS layouts, multi-step flows, data-entry screens, navigation systems, modals, and any request to "design", "build", "create", or "style" a user-facing interface. Use this skill even when the user only mentions aesthetics — because good-looking UI without good UX is a trap.
---

This skill guides the creation of frontend interfaces that are both visually exceptional **and** genuinely usable. Design and UX are treated as inseparable: a beautiful interface that confuses users has failed. An ugly interface that works perfectly has also failed. The goal is both.

Read this entire document before writing a single line of code.

---

## Phase 1: Understand Before You Design

Before any aesthetic or technical decision, answer these questions:

### Who is the user?
- What is their context (rushed, focused, occasional, expert, first-time)?
- What device are they likely on?
- What emotional state do they arrive in (anxious, curious, hurried, skeptical)?

### What is the job to be done?
- What is the user trying to accomplish? (Not: what does the UI display — what does the *user* want to *do*?)
- What is the single most important action on this screen?
- What would make this interaction feel effortless?

### What can go wrong?
- What errors or edge cases are likely?
- What happens if the user does something unexpected?
- What does empty state look like? Loading? Failure?

> **Only after answering these should you pick fonts, colors, or layouts.**

---

## Phase 2: UX Logic — The Rules

### 2.1 Hierarchy of Attention

Every screen has one primary action. Design for it ruthlessly.

- **One hero action per view.** Don't put three equally weighted buttons. Pick one.
- **Visual weight = priority.** The most important thing should be largest, most colorful, or most central. Secondary actions are visually subordinate.
- **Reduce cognitive load.** Show only what's needed for the current step. Progressive disclosure > information overload.

### 2.2 Feedback & System Status

Users must always know what's happening.

| State | What to show |
|---|---|
| Loading | Skeleton screens or spinners with context ("Saving your work…") |
| Success | Confirmation — specific, not generic ("Payment sent to Maya ✓") |
| Error | What went wrong + how to fix it. Never just "Error occurred." |
| Empty | Explain why it's empty + a clear path forward (not just a blank div) |
| Disabled | Why it's disabled, if non-obvious. Don't hide — grey out with explanation. |

Always implement all five states, not just the happy path.

### 2.3 Forms & Data Entry

Forms are where users abandon products. Design them carefully.

- **Label above field, always.** Placeholder text is not a label — it disappears on focus.
- **Inline validation, not on submit.** Validate each field when the user leaves it (onBlur), not all-at-once on submit.
- **Error messages next to the error.** Never only at the top. The user's eye is at the field.
- **Make the next step obvious.** After a form submit, what happens? Show it.
- **Keyboard-first.** Tab order must be logical. Enter should submit. Escape should cancel modals.
- **Autofocus the first field** when a form or modal opens.
- **Don't reset forms on error.** Never clear what the user typed just because one field failed.

### 2.4 Navigation & Wayfinding

Users must always know where they are and how to get where they want to go.

- **Active state** on nav items must be visually distinct and unambiguous.
- **Breadcrumbs** for anything deeper than 2 levels.
- **Back behavior**: destructive back (losing form data) must warn the user.
- **Mobile navigation**: hamburger menus are a last resort. Prefer bottom nav bars for frequent actions.
- **Don't orphan users**: 404 pages, empty states, and error screens must have a path back.

### 2.5 Interaction Design

- **Affordance**: clickable things must look clickable. Interactive elements need hover/active states.
- **Targets**: minimum 44×44px touch targets on mobile. Never make users tap a 12px link.
- **Hover states** on all interactive elements — cursors too (`cursor: pointer`).
- **Transitions**: 150–300ms for UI feedback (hover, toggle). 300–500ms for larger transitions (modal open, page load). Never animate things the user didn't ask to move.
- **Destructive actions** (delete, cancel, log out) must require confirmation. Make the confirm button red. Make the cancel button the default.
- **Loading buttons**: disable the button and show a spinner inline when async actions are in flight. Never let users double-submit.

### 2.6 Accessibility (Non-Negotiable)

Accessibility is not optional. These are minimums:

- **Color contrast**: 4.5:1 for body text, 3:1 for large text and UI components (WCAG AA).
- **Never communicate with color alone**: error states need an icon or text, not just a red border.
- **Focus rings**: never do `outline: none` without a custom replacement. Keyboard users must see where they are.
- **Semantic HTML**: use `<button>` for buttons, `<a>` for links, `<label>` for form labels. Don't make `<div>`s interactive.
- **ARIA labels** on icon-only buttons and form elements without visible labels.
- **Alt text** on meaningful images. `alt=""` on decorative ones.
- **Screen reader–friendly**: modals should trap focus. Toasts/notifications should be announced via `role="alert"` or `aria-live`.

### 2.7 Mobile & Responsive Design

Design mobile-first when in doubt.

- **Breakpoints**: think in content breakpoints, not device breakpoints. Break when the layout breaks.
- **Touch targets**: 44px minimum.
- **No hover-dependent interactions on mobile.** Hover tooltips, hover menus — these don't exist on touch.
- **Font sizes**: minimum 16px for body copy on mobile to prevent auto-zoom on iOS.
- **Stacking behavior**: columns collapse top-to-bottom. The most important content first.
- **Fixed bottom bars** for primary actions on mobile (e.g., "Add to Cart") — keep them accessible.

---

## Phase 3: Visual Design — The Aesthetic Rules

### 3.1 Commit to a Direction

Before writing code, choose a conceptual aesthetic and execute it with precision. The sin is not picking the wrong style — it is being **undecided**. Muddled design reads as amateur.

Choose a pole and go there:
- Brutally minimal / Swiss grid / editorial
- Maximalist / layered / textural / expressive
- Retro-futuristic / skeuomorphic revival
- Organic / natural / soft
- Luxury / refined / high-end
- Industrial / utilitarian / functional
- Playful / toy-like / whimsical
- Dark and technical / developer tool aesthetic
- Academic / archival / typographic
- Art deco / geometric / decorative

Commit. Half-measures produce forgettable interfaces.

### 3.2 Typography

Typography carries more of the aesthetic load than anything else. Choose carefully.

- **Avoid**: Inter, Roboto, Arial, system-ui as primary display fonts. These are neutral to the point of invisibility.
- **Prefer**: fonts with character. Clash Display, Syne, DM Serif Display, Playfair Display, Space Mono, Bebas Neue, Cormorant Garamond, Cabinet Grotesk, Fraunces, Plus Jakarta Sans, Bricolage Grotesque, Instrument Serif — and dozens more from Google Fonts.
- **Pair**: a distinctive display/heading font with a more neutral body font.
- **Scale**: use a clear type scale. Don't have 6 font sizes that are all within 2px of each other.
- **Line-height**: 1.5–1.7 for body, 1.1–1.3 for headings.
- **Measure**: 60–75 characters per line for comfortable reading.

### 3.3 Color

- **Commit to a palette.** Use CSS custom properties (variables) for every color.
- **Dominant + accent**: one or two dominant colors, one sharp accent. Don't distribute 6 colors equally.
- **Test in context**: ensure sufficient contrast at every pairing. Don't just pick pretty colors in isolation.
- **Purple gradients on white are banned.** So are default Tailwind color palettes used without modification.

### 3.4 Spacing & Layout

- **Use a base unit**: 4px or 8px grid. Every margin, padding, and gap should be a multiple.
- **Generous whitespace** around key elements. Don't crowd the primary action.
- **Asymmetry can be powerful** — don't default to centered everything.
- **Unexpected layouts**: diagonal sections, overlapping elements, grid-breaking features — these are memorable. Use deliberately.

### 3.5 Motion

- **Page load**: one well-orchestrated entrance animation with staggered reveals beats ten scattered micro-interactions.
- **Hover states**: every interactive element responds to hover.
- **Transitions**: `transition: all` is lazy and causes jank. Specify the property (`transition: background-color 200ms ease`, `transition: transform 300ms ease`).
- **Prefer CSS animations** for HTML artifacts. Use the Motion/Framer Motion library in React when available.
- **Don't animate things users didn't touch.** Autoplaying carousels, perpetually spinning loaders, ambient particle systems — these distract without helping.
- **Respect `prefers-reduced-motion`**: wrap non-essential animations in `@media (prefers-reduced-motion: no-preference)`.

### 3.6 Visual Depth & Atmosphere

Go beyond flat colors:

- Gradient meshes, noise textures, grain overlays
- Layered transparencies and backdrop blur (`backdrop-filter: blur()`)
- Dramatic, intentional box shadows (not default browser shadows)
- Decorative borders, ruled lines, geometric accents
- Background patterns (SVG-based, CSS-based)
- Custom cursor styles where appropriate and on-brand

---

## Phase 4: Implementation Standards

### 4.1 Component Structure (React)

```jsx
// Always: default export, no required props, hooks at top
export default function ComponentName() {
  const [state, setState] = useState(initialValue);

  // handlers grouped together
  const handleAction = () => { ... };

  // render
  return ( ... );
}
```

- Keep components under ~150 lines. Extract sub-components if longer.
- Co-locate state with the component that needs it. Lift only when necessary.
- Use `useCallback` for handlers passed to child components.
- Memoize expensive renders with `React.memo` or `useMemo` when measurable.

### 4.2 CSS Architecture

For HTML artifacts:

```css
:root {
  /* Colors */
  --color-primary: #1a1a2e;
  --color-accent: #e94560;
  --color-surface: #f8f6f0;
  --color-text: #1a1a2e;
  --color-text-muted: #6b6b7b;

  /* Typography */
  --font-display: 'Fraunces', serif;
  --font-body: 'Plus Jakarta Sans', sans-serif;
  --text-xs: 0.75rem;
  --text-sm: 0.875rem;
  --text-base: 1rem;
  --text-lg: 1.125rem;
  --text-xl: 1.25rem;
  --text-2xl: 1.5rem;
  --text-4xl: 2.25rem;

  /* Spacing */
  --space-1: 0.25rem;
  --space-2: 0.5rem;
  --space-4: 1rem;
  --space-6: 1.5rem;
  --space-8: 2rem;
  --space-12: 3rem;
  --space-16: 4rem;

  /* Transitions */
  --transition-fast: 150ms ease;
  --transition-base: 250ms ease;
  --transition-slow: 400ms ease;
}
```

Always define these before any component-specific styles.

### 4.3 All States Required

Every interactive component must implement:

```jsx
// Required states for every async action
const [status, setStatus] = useState('idle'); // idle | loading | success | error
const [errorMessage, setErrorMessage] = useState('');

// Required visual states for every input
// - default
// - focus
// - filled
// - error
// - disabled
```

Never ship a component with only a happy-path state.

### 4.4 Accessibility Implementation

```html
<!-- Buttons -->
<button
  type="button"
  aria-label="Close dialog"
  onClick={handleClose}
>
  <XIcon aria-hidden="true" />
</button>

<!-- Forms -->
<label htmlFor="email">Email address</label>
<input
  id="email"
  type="email"
  aria-describedby="email-error"
  aria-invalid={hasError}
/>
{hasError && (
  <span id="email-error" role="alert">
    Please enter a valid email address.
  </span>
)}

<!-- Toasts / notifications -->
<div role="alert" aria-live="polite">
  {notification}
</div>
```

### 4.5 Responsive Breakpoints

```css
/* Mobile first */
.component { ... }

/* Tablet */
@media (min-width: 640px) { ... }

/* Desktop */
@media (min-width: 1024px) { ... }

/* Wide */
@media (min-width: 1280px) { ... }
```

For Tailwind: `sm:` `md:` `lg:` `xl:` prefixes map to these.

---

## Phase 5: The Pre-Ship Checklist

Before declaring done, verify:

**UX**
- [ ] Primary action is visually dominant and obvious
- [ ] All five states implemented: default, loading, success, error, empty
- [ ] Inline form validation with specific error messages
- [ ] Keyboard navigation works (tab order, enter to submit, escape to close)
- [ ] Destructive actions have a confirmation step
- [ ] Mobile touch targets are ≥44px

**Accessibility**
- [ ] Color contrast passes WCAG AA (4.5:1 body, 3:1 large)
- [ ] No information conveyed by color alone
- [ ] Focus rings are visible
- [ ] Semantic HTML used throughout
- [ ] Icon-only buttons have aria-label
- [ ] Form inputs are labeled

**Visual Design**
- [ ] CSS variables defined for all tokens
- [ ] Consistent spacing from an 8px grid
- [ ] All interactive elements have hover/active states
- [ ] Transitions specified by property (not `transition: all`)
- [ ] Typography uses a distinct, characterful font choice
- [ ] No purple gradients on white backgrounds, no generic Tailwind defaults

**Responsive**
- [ ] Readable and functional at 375px (mobile) through 1440px (wide desktop)
- [ ] No horizontal overflow on mobile
- [ ] Font size ≥16px for body copy on mobile

---

## Common Patterns Reference

### Loading Button
```jsx
<button
  onClick={handleSubmit}
  disabled={isLoading}
  className={`btn-primary ${isLoading ? 'opacity-70 cursor-not-allowed' : ''}`}
>
  {isLoading ? (
    <><Spinner size={16} /> Saving…</>
  ) : 'Save Changes'}
</button>
```

### Form Field with Error
```jsx
<div className="field-group">
  <label htmlFor={id}>{label}</label>
  <input
    id={id}
    type={type}
    value={value}
    onChange={onChange}
    onBlur={onBlur}
    aria-invalid={!!error}
    aria-describedby={error ? `${id}-error` : undefined}
    className={error ? 'input-error' : 'input'}
  />
  {error && (
    <span id={`${id}-error`} className="error-message" role="alert">
      {error}
    </span>
  )}
</div>
```

### Empty State
```jsx
<div className="empty-state" role="region" aria-label="No results">
  <EmptyIllustration />
  <h3>No items yet</h3>
  <p>Once you add items, they'll appear here.</p>
  <button onClick={onAdd} className="btn-primary">Add your first item</button>
</div>
```

### Confirmation Dialog (Destructive)
```jsx
<dialog aria-modal="true" aria-labelledby="dialog-title">
  <h2 id="dialog-title">Delete this item?</h2>
  <p>This action cannot be undone.</p>
  <div className="dialog-actions">
    <button onClick={onClose} className="btn-secondary" autoFocus>
      Cancel
    </button>
    <button onClick={onConfirm} className="btn-danger">
      Delete
    </button>
  </div>
</dialog>
```

---

## What Makes This Different

Most AI-generated UI fails in two distinct ways:
1. **Aesthetically:** Generic fonts, predictable layouts, safe color choices, no point of view.
2. **Functionally:** Only the happy path is designed. Error states are an afterthought. Accessibility is ignored. Mobile is a shrunken desktop.

This skill refuses both failures. Every output should be something you'd be proud to show in a portfolio **and** something you'd trust to deploy to real users.

**The standard**: if a senior product designer and a senior frontend engineer both reviewed the output, neither would have major objections.
