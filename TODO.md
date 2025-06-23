- [ ] Fix deletes on Many-to-Many relations
    - Eg: deleting a category doesn't work if any entries are using it, ideally we should just remove the categories
      from
      the entry (delete the join from the joining table)
- [X] Invalidate ALL data from the UI when logout or force logout happens
- [ ] Integrate UI with SSE
- [ ] Update chat category cache when category added/edited, etc.
- [ ] Use AI to summarize large conversations to make it more cost-efficient
