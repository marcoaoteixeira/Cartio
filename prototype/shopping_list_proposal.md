1. Header
    Elements: * Left: Navigation "Back" arrow.

        Center: List Title (e.g., "Weekly Groceries") with an Edit (Pencil) Icon. Underneath, a sub-label shows list status (e.g., "2 of 8 picked up" or "Empty list").
        Right: Search icon and a Vertical Ellipsis (Kebab menu) for settings.

    Progress Indicator: A thin yellow progress bar is docked at the very bottom of the orange header to visualize "Items Picked Up" vs. "Total Items."

2. Add List Item Fixed Input Bar (Bottom)
    Container: Floating white pill-shaped bar with a soft drop shadow.
    Left: "Add to Cart" icon.
    Center: Input field with placeholder text "Add item...".
    Right: A circular "+" button for quick entry.

3. Screen State: Empty Shopping List (empty_shopping_list.png)
Central Placeholder

    Illustration: Stylized document/list icon with a floating orange "+" badge.

    Messaging:
        Title: "Nothing here yet" (Bold, 18pt-20pt).
        Body: Instructional text explaining how to add items and quantities.

    Primary CTA: * Label: "Add your first item".
        Icon: Keyboard icon on the left.
        Style: Rounded pill button, Primary Orange, with a significant drop shadow.

4. Screen State: Ongoing Shopping List (ongoing_shopping_list.png)
Active Items Section

    Layout: Vertical list of rows with subtle dividers.

    Row Composition:
        Left: Unchecked radio-style circle (Empty).
        Center: Item Name (e.g., "Eggs, free range").
        Right (Steppers): * Minus (-) button.
            Quantity (Bold number).
            Plus (+) button.
            Note: If the quantity is 1, the "Minus" button may transform into a Red Trash Can icon to signify deletion.

Completed Items Section ("DONE")

    Header: A "DONE · [Count]" label with a horizontal line spanning the width.

    Row Changes:
        Left: Checked circle (Orange background with white checkmark).
        Text Style: Item name is struck through and changed to a light gray color.
        Right (Steppers): The quantity and stepper buttons are desaturated/ghosted to indicate they are inactive or secondary.

5. Proposed UX Logic & Interactions

    Swipe Actions:
		- Swiping left on an active item reveal the trash icon and triggers delete.
		- Swiping right on an active item reveal the check icon and triggers checked.

    Checkbox Toggle:
		- Tapping the circle or the item row should move the item between the "Active" and "Done" sections with a smooth transition animation.

    Edit Mode: Tapping the Pencil icon in the header should allow the user to rename the list.