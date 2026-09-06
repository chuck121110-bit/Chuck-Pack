# AGENTS - Persistent Instructions (MUST READ EVERY TURN)

## CRITICAL: Do Not Change Stuff Randomly
- **NEVER** change, refactor, or "improve" code, logic, icons, categories, or any pack behavior unless the user explicitly requests that specific change in that turn.
- The user called out earlier random changes to category icons (showing CHLIS before world load) and other logic - this must not happen again.
- Always re-read this file at the start of every session/turn before making any edits.

## Finished/Good Log - DO NOT TOUCH WITHOUT CONFIRMATION
- When the user says something is `good`, `finished`, `done`, `perfect`, or `approved`, you MUST log it here and then **never** modify that feature/file/logic again.
- If the user later asks to change something on this list, you MUST first confirm: "This was previously marked as finished/good on [date]. Are you sure you want to modify it? Confirm you mean to change it."
- Only touch it again after explicit confirmation that they mean to change a finished item.

### Finished Items (Do Not Touch)
- (none logged yet - will be appended as user confirms)

## Workflow
1. Before any edit, read AGENTS.md + FINISHED_LOG.md
2. Only do exactly what the user asked for in the current turn, no extra "fixes" or "improvements"
3. If user says a feature is good/finished, append it to FINISHED_LOG.md with date and description
4. Before editing any file that is on the finished list, ask for confirmation as above

## User Corrections (Active)
- Category icons: must always be an icon (`new ItemStack(Items.EMERALD)`) not text, never change icon rendering logic randomly - fixed 2026-09-05
- VillagerRoller is now 1-to-1 from `villager-roller-1.4.19+mc26.1.2-build.47.zip` - do not modify without explicit request
- BaseFinder: `Auto delete flagged chunks` + `Delete time`/`Delete radius` + triggers `x` counts are approved as implemented - do not change

