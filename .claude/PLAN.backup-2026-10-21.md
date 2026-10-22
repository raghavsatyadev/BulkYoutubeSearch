# Plan
Goal: BulkYoutubeSearch — Hollywood video relink finished; app on Compose M3E (master d169ace).
Resume: full task list + commands in `tmp/relink/PLAN.md` (written 2026-10-13).

## Now
- [playlist] HollyFavs sync APPLYING (started 2026-10-21 ~00:50, bg run of scratchpad sync_playlist.py --apply --yes): 114/114 deletes OK, adds in progress (8/124 at 01:11). Check `grep SYNC tmp/playlist_run.log | tail` for `SYNC END ... exact=True`; if left>0 (quota), re-run the same command next day.
- [playlist] Liked songs → HollyFavs: user chose all liked songs (2026-10-21). 12 added to tmp/relink/keep_no_file.json (source=liked; Still Falling For You, I'm a Mess, The Monster skipped, already in list as official videos); video_links.json rebuilt 652→664 (backups *.before_liked.json). A second `sync_playlist.py --apply` is queued in the background to start after the first run ends (+12 adds, 0 deletes). Check for a second `SYNC END ... exact=True` in tmp/playlist_run.log.
- [relink] 565/629 videos renamed + tagged; 64 left. YouTube search quota (~100/day per key, 4 keys) resets 12:30 IST.
  - Re-check the 11 dropped videos for an official upload (rows 26, 30, 66, 102, 161, 186, 187, 373, 488, 496, 538 in names.json).
  - Re-search official uploads for re-upload / fan-lyric picks (list in tmp/relink/PLAN.md tasks 2–4).
  - Apply naming decisions → `apply.py --dry`, apply, `update_links.py`. Checked 2026-10-18: 0/55 "Your name" cells filled.

## Next
- [relink] Duplicates: rows 408, 469 (second copies), 307 (Starving lyric video) — delete or keep as "(2)".
- [relink] After user is happy: delete `D:/Media/Videos/Hollywood_originals_2026-10-12` (~43 GB; still present 2026-10-18).
- Local branches docs/per-project-android-skills and feat/found-songs-search-import are merged (PRs #1, #2); delete when convenient.

## Blocked / Questions
- DECIDE: [relink] naming_decisions.md (tmp/relink) — user fills the "Your name" column for 53 videos (blank = accept proposal, `skip` = leave file). Blocks task 5 of tmp/relink/PLAN.md.

## Notes
- Migrated 2026-10-18 from `.remember/` (removed); old journal in `.claude/HISTORY.md`.
- video_links.json lives in Drive `GoogleDrive/Raghav/Work/Projects/BYS/` (618 entries); backups beside it.

## Log
- 2026-10-18: plan created from .remember notes + tmp/relink/PLAN.md.
