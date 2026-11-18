import { Link } from 'react-router'
import type { RecordSummary } from '../api/records'
import { displayTitle, formatDateTime } from '../records/format'
import { findMode } from '../records/modes'

type RecordTableProps = {
  records: RecordSummary[]
  /** 作成日時の列を表示するか（記録一覧では表示し、ダッシュボードでは表示しない） */
  showCreatedAt: boolean
}

/** 記録の一覧表（SC-03 最近の記録、SC-06 記録一覧）。タイトルから詳細画面へ移動できる */
// 省略しない形：
// export function RecordTable(props: RecordTableProps) {
//   const records = props.records
//   const showCreatedAt = props.showCreatedAt
export function RecordTable({ records, showCreatedAt }: RecordTableProps) {
  return (
    <div className="table-wrapper">
      <table className="record-table">
        <thead>
          <tr>
            <th>対象日</th>
            <th>タイトル</th>
            <th>モード</th>
            {/* showCreatedAt が true のときだけ表示する。省略しない形：{showCreatedAt ? <th>作成日時</th> : null} */}
            {showCreatedAt && <th>作成日時</th>}
            <th>更新日時</th>
          </tr>
        </thead>
        <tbody>
          {/* map：配列の1件ずつを <tr>（表の1行）に変換する。Java の stream().map() と同じ考え方 */}
          {records.map((record) => (
            <tr key={record.id}>
              <td>{record.targetDate}</td>
              <td>
                <Link to={`/records/${record.id}`}>{displayTitle(record.title)}</Link>
              </td>
              <td>{findMode(record.mode).label}</td>
              {showCreatedAt && <td>{formatDateTime(record.createdAt)}</td>}
              <td>{formatDateTime(record.updatedAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
